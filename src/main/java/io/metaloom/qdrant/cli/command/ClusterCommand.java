package io.metaloom.qdrant.cli.command;

import picocli.CommandLine.Command;

@Command(name = "cluster", aliases = { "c" }, description = "Cluster commands. (e.g. peer management, info)")
public class ClusterCommand extends AbstractQDrantCommand {

	@Command(name = "info", description = "Get information about the current state and composition of the cluster")
	public int info() {
		try {
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 10;
		}
	}

	@Command(name = "remove-peer", description = "Tries to remove peer from the cluster. Will return an error if peer has shards on it.")
	public int removePeer() {
		try {
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 10;
		}

	}
}
