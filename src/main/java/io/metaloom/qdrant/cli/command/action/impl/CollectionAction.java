package io.metaloom.qdrant.cli.command.action.impl;

import static io.metaloom.qdrant.cli.ExitCode.FILE_ERROR;
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
import io.metaloom.qdrant.client.http.QDrantHttpClient;
import io.metaloom.qdrant.client.http.impl.HttpErrorException;
import io.metaloom.qdrant.client.http.model.collection.CollectionDescription;
import io.metaloom.qdrant.client.http.model.collection.CollectionListResponse;
import io.metaloom.qdrant.client.http.model.collection.CollectionResponse;
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

				long totalWritten = 0;
				if (outputPath.equals("-")) {
					try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out))) {
						totalWritten = dump(client, collections, writer);
					}
				} else {
					File outputFile = new File(outputPath);
					if (outputFile.exists()) {
						log.error("Output file already inplace.");
						return FILE_ERROR;
					}
					try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
						totalWritten = dump(client, collections, writer);
					}
					log.info("Backup of {} collections written to {}", totalWritten, outputPath);
				}

				return OK;
			} else {
				log.error("Listing collections failed with status [{}]", response.getStatus());
				return SERVER_FAILURE;
			}
		});
	}

	private long dump(QDrantHttpClient client, List<CollectionDescription> collections, BufferedWriter writer)
			throws IOException, HttpErrorException {
		long totalWritten = 0;
		for (CollectionDescription collection : collections) {
			String collectionName = collection.getName();
			if (log.isDebugEnabled()) {
				log.debug("Loading collection {}", collectionName);
			}
			CollectionResponse response = client.loadCollection(collectionName).sync();
			if (isSuccess(response)) {
				String json = Json.parseCompact(response.getResult());
				writer.append(json + "\n");
				totalWritten++;
			} else {
				log.error("Error while loading info for collection " + collectionName + ". Got status: "
						+ response.getStatus());
				throw new RuntimeException("Error while loading collection " + collectionName);
			}

		}
		return totalWritten;
	}

}
