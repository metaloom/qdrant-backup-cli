package io.metaloom.qdrant.cli.command.action.impl;

import static io.metaloom.qdrant.cli.ExitCode.OK;
import static io.metaloom.qdrant.cli.ExitCode.SERVER_FAILURE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.qdrant.cli.ExitCode;
import io.metaloom.qdrant.cli.command.QDrantCommand;
import io.metaloom.qdrant.cli.command.action.AbstractAction;
import io.metaloom.qdrant.client.http.model.collection.CollectionDescription;
import io.metaloom.qdrant.client.http.model.collection.CollectionListResponse;
import io.metaloom.qdrant.client.json.Json;

public class CollectionAction extends AbstractAction {

	public static final Logger log = LoggerFactory.getLogger(CollectionAction.class);

	public CollectionAction(QDrantCommand command) {
		super(command);
	}

	public ExitCode backup(int batchSize, String outputPath) {
		return withClient(client -> {
			CollectionListResponse response = client.listCollections().sync();
			if (isSuccess(response)) {
				List<CollectionDescription> collections = response.getResult().getCollections();

				if (outputPath.equals("-")) {
					try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out))) {
						dump(collections, writer);
					}
				} else {
					File outputFile = new File(outputPath);
					// if (!outputFile.canWrite()) {
					// log.error("Unable to write to " + outputFile.getAbsolutePath());
					// System.exit(12);
					// }
					try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
						dump(collections, writer);
					}
				}

				return OK;
			} else {
				log.error("Listing collections failed with status [{}]", response.getStatus());
				return SERVER_FAILURE;
			}
		});
	}

	private void dump(List<CollectionDescription> collections, BufferedWriter writer) throws IOException {
		for (CollectionDescription collection : collections) {
			String json = Json.parseCompact(collection);
			writer.append(json + "\n");
		}
	}

}
