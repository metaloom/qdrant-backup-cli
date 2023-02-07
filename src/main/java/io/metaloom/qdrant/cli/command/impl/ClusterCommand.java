package io.metaloom.qdrant.cli.command.impl;

import static io.metaloom.qdrant.cli.ExitCode.ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.qdrant.cli.command.AbstractQDrantCommand;
import io.metaloom.qdrant.cli.command.action.impl.ClusterAction;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "cluster", aliases = { "cl" }, description = "Cluster commands. (e.g. peer management, info)")
public class ClusterCommand extends AbstractQDrantCommand {

	public static final Logger log = LoggerFactory.getLogger(ClusterCommand.class);

	@Command(name = "info", description = "Get information about the current state and composition of the cluster")
	public int info() {
		try {
			return new ClusterAction(this).info().code();
		} catch (Exception e) {
			log.error("Fetching info failed.", e);
			return ERROR.code();
		}
	}

	@Command(name = "remove-peer", description = "Tries to remove peer from the cluster. Will return an error if peer has shards on it.")
	public int removePeer(@Parameters(index = "0", description = "Id of the peer which should be removed from the cluster.") String  peerId) {
		try {
			return new ClusterAction(this).removePeer(peerId).code();
		} catch (Exception e) {
			log.error("Removing peer failed", e);
			return ERROR.code();
		}

	}
}
