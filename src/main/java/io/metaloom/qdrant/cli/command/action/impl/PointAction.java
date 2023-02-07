package io.metaloom.qdrant.cli.command.action.impl;

import static io.metaloom.qdrant.cli.ExitCode.FILE_ERROR;
import static io.metaloom.qdrant.cli.ExitCode.INVALID_PARAMETER;
import static io.metaloom.qdrant.cli.ExitCode.OK;
import static io.metaloom.qdrant.cli.ExitCode.SERVER_FAILURE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.qdrant.cli.ExitCode;
import io.metaloom.qdrant.cli.command.QDrantCommand;
import io.metaloom.qdrant.cli.command.action.AbstractAction;
import io.metaloom.qdrant.cli.eta.ETAUtil;
import io.metaloom.qdrant.client.http.QDrantHttpClient;
import io.metaloom.qdrant.client.http.model.point.PointCountRequest;
import io.metaloom.qdrant.client.http.model.point.PointCountResponse;
import io.metaloom.qdrant.client.http.model.point.PointsScrollRequest;
import io.metaloom.qdrant.client.http.model.point.PointsScrollResponse;
import io.metaloom.qdrant.client.http.model.point.Record;
import io.metaloom.qdrant.client.http.model.point.ScrollResult;
import io.metaloom.qdrant.client.json.Json;

public class PointAction extends AbstractAction {

	public static final Logger log = LoggerFactory.getLogger(PointAction.class);

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
		log.info("[" + ETAUtil.getPercent(totalCount, totalCount) + "]");
		writer.flush();
		return current;

	}

}
