package io.metaloom.qdrant.cli.command.action.impl;

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
			if (outputPath.equals("-")) {
				try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out))) {
					scrollPoints(client, writer, collectionName, batchSize);
				}
			} else {
				File outputFile = new File(outputPath);
				// if (!outputFile.canWrite()) {
				// log.error("Unable to write to " + outputFile.getAbsolutePath());
				// System.exit(12);
				// }
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
					scrollPoints(client, writer, collectionName, batchSize);
				}
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

	private void scrollPoints(QDrantHttpClient client, BufferedWriter writer, String collectionName, int batchSize)
			throws Exception {
		Long offset = 0L;
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
		writer.flush();

	}

}
