package io.metaloom.qdrant.cli.command.action.impl;

import static io.metaloom.qdrant.cli.ExitCode.ERROR;
import static io.metaloom.qdrant.cli.ExitCode.FILE_ERROR;
import static io.metaloom.qdrant.cli.ExitCode.INVALID_PARAMETER;
import static io.metaloom.qdrant.cli.ExitCode.OK;
import static io.metaloom.qdrant.cli.ExitCode.SERVER_FAILURE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.qdrant.cli.ExitCode;
import io.metaloom.qdrant.cli.command.QDrantCommand;
import io.metaloom.qdrant.cli.command.action.AbstractAction;
import io.metaloom.qdrant.cli.eta.ETAUtil;
import io.metaloom.qdrant.client.http.QDrantHttpClient;
import io.metaloom.qdrant.client.http.model.point.PointCountRequest;
import io.metaloom.qdrant.client.http.model.point.PointCountResponse;
import io.metaloom.qdrant.client.http.model.point.PointStruct;
import io.metaloom.qdrant.client.http.model.point.PointsListUpsertRequest;
import io.metaloom.qdrant.client.http.model.point.PointsScrollRequest;
import io.metaloom.qdrant.client.http.model.point.PointsScrollResponse;
import io.metaloom.qdrant.client.http.model.point.Record;
import io.metaloom.qdrant.client.http.model.point.ScrollResult;
import io.metaloom.qdrant.client.json.Json;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class PointAction extends AbstractAction {

	public static final Logger log = LoggerFactory.getLogger(PointAction.class);

	private static final long RESTORE_OPERATION_TIMEOUT_MS = 10_000;

	public PointAction(QDrantCommand command) {
		super(command);
	}

	public ExitCode backup(int batchSize, String collectionName, String outputPath) {
		if (collectionName == null) {
			log.error("Missing collection name");
			return INVALID_PARAMETER;
		}
		if (outputPath == null) {
			log.error("A output path must be specified.");
			return INVALID_PARAMETER;
		}

		if (log.isDebugEnabled()) {
			log.debug("Connecting to {} : {} using {} batch-size: {} ", host, port, collectionName, batchSize);
		}
		return withClient(client -> {
			PointCountRequest request = new PointCountRequest();
			request.setExact(true);
			PointCountResponse response = client.countPoints(collectionName, request).sync();
			if (!isSuccess(response)) {
				log.error("Loading point count of collection {} failed.", collectionName);
				return SERVER_FAILURE;
			}
			long totalCount = response.getResult().getCount();
			long totalWritten = 0;
			if (outputPath.equals("-")) {
				try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out))) {
					totalWritten = scrollPoints(client, writer, collectionName, batchSize, totalCount, false);
				}
			} else {
				File outputFile = new File(outputPath);
				if (outputFile.exists()) {
					log.error("Output file already inplace.");
					return FILE_ERROR;
				}
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
					totalWritten = scrollPoints(client, writer, collectionName, batchSize, totalCount, true);
				}
				log.info("Backup of {} points written to {}", totalWritten, outputPath);
			}
			return OK;
		});

	}

	public ExitCode restore(int batchSize, String collectionName, String inputPath) {
		if (collectionName == null) {
			log.error("Missing collection name");
			return INVALID_PARAMETER;
		}
		if (inputPath == null) {
			log.error("A input path must be specified.");
			return INVALID_PARAMETER;
		}

		if (log.isDebugEnabled()) {
			log.debug("Connecting to {} : {} using {} batch-size: {} ", host, port, collectionName, batchSize);
		}
		if (inputPath.equals("-")) {
			// TODO check for EOF
			try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
				Stream<String> stream = in.lines();
				return restore(stream, collectionName, 0, batchSize);
			} catch (IOException e) {
				log.error("Error while running restore", e);
				return ERROR;
			}
		} else {
			Path input = Paths.get(inputPath);
			if (!Files.exists(input)) {
				log.error("Could not find file " + inputPath);
				return INVALID_PARAMETER;
			}
			try {
				long total = countLines(input);
				try (Stream<String> lines = Files.lines(input, StandardCharsets.UTF_8)) {
					return restore(lines, collectionName, total, batchSize);
				}
			} catch (Exception e) {
				log.error("Error while running restore", e);
				return ERROR;
			}
		}

	}

	private ExitCode restore(Stream<String> feed, String collectionName, long total, int batchSize) {
		return withClient(client -> {
			// Form batches and submit them to the server
			AtomicLong batchesCompleted = new AtomicLong();
			long batchesNeeded = total / batchSize;
			Observable.fromStream(feed)
				.map(this::toPoint)
				.buffer(batchSize)
				.map(batch -> submitBatch(client, collectionName, batch, total))
				.blockingSubscribe(c -> {
					c.blockingAwait(RESTORE_OPERATION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
					long current = batchesCompleted.incrementAndGet();
					if (current * batchSize % 500 == 0) {
						log.info("[" + ETAUtil.getPercent(current, batchesNeeded) + "] written");
					}
				}, err -> {
					log.error("Failed to upsert points", err);
				});
			log.info("[100%] written");
			return OK;
		});

	}

	private PointStruct toPoint(String json) {
		try {
			return Json.parse(json, PointStruct.class);
		} catch (Exception e) {
			log.error("Error while deserializing point from json line: {}", json, e);
			throw new RuntimeException(e);
		}
	}

	private long countLines(Path input) throws IOException {
		try (Stream<String> lines = Files.lines(input, StandardCharsets.UTF_8)) {
			return lines.count();
		}
	}

	public Completable submitBatch(QDrantHttpClient client, String collectionName, List<PointStruct> batch, long total) {
		return Completable.defer(() -> {
			try {
				if (log.isDebugEnabled()) {
					log.debug("Preparing upsert request with {} elements.", batch.size());
				}
				PointsListUpsertRequest request = new PointsListUpsertRequest();
				for (PointStruct point : batch) {
					request.addPoint(point);
				}
				return client.upsertPoints(collectionName, request, false).async().ignoreElement();
			} catch (Exception e) {
				return Completable.error(e);
			}
		});
	}

	public ExitCode count(String collectionName, boolean exact) {
		if (collectionName == null) {
			log.error("Missing collection name");
			return INVALID_PARAMETER;
		}
		return withClient(client -> {
			PointCountRequest request = new PointCountRequest();
			request.setExact(exact);
			PointCountResponse response = client.countPoints(collectionName, request).sync();
			if (isSuccess(response)) {
				long count = response.getResult().getCount();
				log.info("Points: " + count);
				return OK;
			} else {
				log.error("Loading point count failed with status [{}]", response.getStatus());
				return SERVER_FAILURE;
			}
		});
	}

	private long scrollPoints(QDrantHttpClient client, BufferedWriter writer, String collectionName, int batchSize,
		long totalCount, boolean printETA) throws Exception {
		long current = 0;
		Long offset = 0L;
		long start = System.currentTimeMillis();
		while (true) {
			PointsScrollRequest request = new PointsScrollRequest();
			request.setLimit(batchSize);
			request.setOffset(offset);
			request.setWithPayload(true);
			request.setWithVector(true);
			PointsScrollResponse response = client.scrollPoints(collectionName, request).sync();
			if (isSuccess(response)) {
				ScrollResult result = response.getResult();
				if (result.getPoints().isEmpty()) {
					break;
				}
				for (Record point : result.getPoints()) {
					String json = Json.parseCompact(point);
					// Append NDJson
					writer.append(json + "\n");
					if (log.isTraceEnabled()) {
						log.trace(json);
					}
					current++;
					if (printETA && current % 10_000 == 0) {
						log.info("[" + ETAUtil.getPercent(current, totalCount) + "] "
							+ ETAUtil.getETA(current, totalCount, start, System.currentTimeMillis()));
					}
				}
				writer.flush();
				offset = result.getNextPageOffset();
				if (offset == null) {
					break;
				}
			} else {
				log.error("Scroll request failed with status: [{}]", response.getStatus());
				break;
			}
		}
		log.info("[" + ETAUtil.getPercent(totalCount, totalCount) + "] read");
		writer.flush();
		return current;

	}

}
