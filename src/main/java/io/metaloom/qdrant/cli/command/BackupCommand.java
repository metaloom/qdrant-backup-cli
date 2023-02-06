package io.metaloom.qdrant.cli.command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.qdrant.client.http.QDrantHttpClient;
import io.metaloom.qdrant.client.http.model.point.PointsScrollRequest;
import io.metaloom.qdrant.client.http.model.point.PointsScrollResponse;
import io.metaloom.qdrant.client.http.model.point.Record;
import io.metaloom.qdrant.client.http.model.point.ScrollResult;
import io.metaloom.qdrant.client.json.Json;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "backup", aliases = { "b" }, description = "Backup commands. (e.g. NDJson dump of points/collections)")
public class BackupCommand extends AbstractQDrantCommand {

	public static final Logger log = LoggerFactory.getLogger(BackupCommand.class);

	@Command(name = "collections", description = "Backup collections")
	public int backupCollections(
		@Option(names = { "-b", "--batch-size" }, description = "Size of the point batches being loaded from the server. Default: "
			+ DEFAULT_BATCH_SIZE, defaultValue = DEFAULT_BATCH_SIZE_STR) int batchSize,
		@Parameters(index = "0", description = "Path to the output file to which the backup will be written. Use - for stdout.") String outputPath) {
		try {
			return 0;
		} catch (Exception e) {
			log.error("Error while running collection backup.", e);
			return 10;
		}
	}

	@Command(name = "points", description = "Backup point data of a collection")
	public int backupPoints(

		@Option(names = { "-c", "--collection" }, description = "Name of the collection to backup") String collectionName,
		@Option(names = { "-b", "--batch-size" }, description = "Size of the point batches being loaded from the server. Default: "
			+ DEFAULT_BATCH_SIZE, defaultValue = DEFAULT_BATCH_SIZE_STR) int batchSize,
		@Parameters(index = "0", description = "Path to the output file to which the backup will be written. Use - for stdout.") String outputPath

	) {
		try {
			if (outputPath == null) {
				log.error("A output path must be specified");
				System.exit(11);
			}
			int port = getPort();
			String host = getHostname();
			if (log.isDebugEnabled()) {
				log.debug("Connecting to " + host + ":" + port + " using " + collectionName + " batch-size: " + batchSize);
			}
			try (QDrantHttpClient client = QDrantHttpClient.builder().setHostname(host).setPort(port).build()) {
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
			}
			return 0;
		} catch (Exception e) {
			log.error("Error while running point backup.", e);
			return 10;
		}
	}

	private void scrollPoints(QDrantHttpClient client, BufferedWriter writer, String collectionName, int batchSize) throws Exception {
		Long offset = 0L;
		while (true) {
			PointsScrollRequest request = new PointsScrollRequest();
			request.setLimit(batchSize);
			request.setOffset(offset);
			request.setWithPayload(true);
			request.setWithVector(true);
			PointsScrollResponse response = client.scrollPoints(collectionName, request).sync();
			if ("ok".equals(response.getStatus())) {
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
				log.error("Scroll request failed with status: " + response.getStatus());
				break;
			}
		}
		writer.flush();

	}
}
