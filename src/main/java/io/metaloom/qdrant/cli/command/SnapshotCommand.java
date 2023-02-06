package io.metaloom.qdrant.cli.command;

import picocli.CommandLine.Command;

@Command(name = "snapshot", aliases = { "snap" }, description = "Snapshot commands. (e.g. create and restore snapshots)")
public class SnapshotCommand extends AbstractQDrantCommand {

	@Command(name = "restore", description = "Restore the snapshot")
	public int restore() {
		try {
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 10;
		}
	}

	@Command(name = "snapshot", description = "Create a new snapshot")
	public int snapshot() {
		try {
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 10;
		}
	}
}
