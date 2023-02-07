package io.metaloom.qdrant.cli.command.impl;

import static io.metaloom.qdrant.cli.ExitCode.ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.qdrant.cli.command.AbstractQDrantCommand;
import io.metaloom.qdrant.cli.command.action.impl.CollectionAction;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "collection", aliases = { "co" }, description = "Collection commands")
public class CollectionCommand extends AbstractQDrantCommand {

	public static final Logger log = LoggerFactory.getLogger(CollectionCommand.class);

	@Command(name = "backup", description = "Backup collections")
	public int backup(
		@Option(names = { "-b", "--batch-size" }, description = "Size of the point batches being loaded from the server. Default: "
			+ DEFAULT_BATCH_SIZE, defaultValue = DEFAULT_BATCH_SIZE_STR) int batchSize,
		@Parameters(index = "0", description = "Path to the output file to which the backup will be written. Use - for stdout.") String outputPath) {
		try {
			return new CollectionAction(this).backup(batchSize, outputPath).code();
		} catch (Exception e) {
			log.error("Error while running collection backup.", e);
			return ERROR.code();
		}
	}
}
