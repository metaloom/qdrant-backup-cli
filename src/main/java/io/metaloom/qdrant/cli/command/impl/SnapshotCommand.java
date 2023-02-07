package io.metaloom.qdrant.cli.command.impl;

import static io.metaloom.qdrant.cli.ExitCode.ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.qdrant.cli.command.AbstractQDrantCommand;
import io.metaloom.qdrant.cli.command.action.impl.SnapshotAction;
import picocli.CommandLine.Command;

@Command(name = "snapshot", aliases = { "s" }, description = "Snapshot commands. (e.g. create and restore snapshots)")
public class SnapshotCommand extends AbstractQDrantCommand {

	public static final Logger log = LoggerFactory.getLogger(SnapshotCommand.class);

	@Command(name = "restore", description = "Restore the snapshot")
	public int restore(String collectionName, String snapshotLocation) {
		try {
			return new SnapshotAction(this).recover(collectionName, snapshotLocation).code();
		} catch (Exception e) {
			log.error("Restoring collections failed.", e);
			return ERROR.code();
		}
	}

	@Command(name = "list", description = "List snapshots for the collection")
	public int list(String collectionName) {
		try {
			return new SnapshotAction(this).list(collectionName).code();
		} catch (Exception e) {
			log.error("Listing collections failed.", e);
			return ERROR.code();
		}
	}

	@Command(name = "snapshot", description = "Create snapshots")
	public int snapshot(String collectionNames) {
		try {
			return new SnapshotAction(this).snapshot(collectionNames).code();
		} catch (Exception e) {
			log.error("Creating snapshots failed", e);
			return ERROR.code();
		}
	}
}
