package io.metaloom.qdrant.cli.command.impl;

import static io.metaloom.qdrant.cli.ExitCode.ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.qdrant.cli.command.AbstractQDrantCommand;
import io.metaloom.qdrant.cli.command.action.impl.PointAction;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "point", aliases = { "p" }, description = "Point commands")
public class PointCommand extends AbstractQDrantCommand {

	public static final Logger log = LoggerFactory.getLogger(PointCommand.class);

	@Command(name = "count", description = "Count the points of the collection.")
	public int count(
		@Option(names = { "-c", "--collection" }, description = "Name of the collection to backup") String collectionName,

		@Option(names = { "-e", "--exact" }, description = "Return the exact values from the server.", defaultValue = "false") Boolean exact) {
		try {
			if (exact == null) {
				exact = false;
			}
			return new PointAction(this).count(collectionName, exact).code();
		} catch (Exception e) {
			log.error("Error while running point backup.", e);
			return ERROR.code();
		}
	}

	@Command(name = "backup", description = "Backup point data of a collection")
	public int backup(

		@Option(names = { "-c", "--collection" }, description = "Name of the collection to backup") String collectionName,
		@Option(names = { "-b", "--batch-size" }, description = "Size of the point batches being loaded from the server. Default: "
			+ DEFAULT_BATCH_SIZE, defaultValue = DEFAULT_BATCH_SIZE_STR) int batchSize,
		@Parameters(index = "0", description = "Path to the output file to which the backup will be written. Use - for stdout.") String outputPath

	) {
		try {
			return new PointAction(this).backup(batchSize, collectionName, outputPath).code();
		} catch (Exception e) {
			log.error("Error while running point backup.", e);
			return ERROR.code();
		}
	}

}
