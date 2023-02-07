package io.metaloom.qdrant.cli.command.action.impl;

import static io.metaloom.qdrant.cli.ExitCode.ERROR;
import static io.metaloom.qdrant.cli.ExitCode.OK;
import static io.metaloom.qdrant.cli.ExitCode.SERVER_FAILURE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.qdrant.cli.ExitCode;
import io.metaloom.qdrant.cli.command.QDrantCommand;
import io.metaloom.qdrant.cli.command.action.AbstractAction;
import io.metaloom.qdrant.client.http.QDrantHttpClient;
import io.metaloom.qdrant.client.http.model.GenericBooleanStatusResponse;
import io.metaloom.qdrant.client.http.model.cluster.ClusterStatusResponse;

public class ClusterAction extends AbstractAction {

	public static final Logger log = LoggerFactory.getLogger(ClusterAction.class);

	public ClusterAction(QDrantCommand command) {
		super(command);
	}

	public ExitCode info() {
		try (QDrantHttpClient client = newClient()) {
			ClusterStatusResponse response = client.getClusterStatusInfo().sync();
			if (isSuccess(response)) {
				System.out.println("Cluster Status: " + response.getResult().getStatus());
				return OK;
			} else {
				log.error("Loading cluster info failed with status [{}]", response.getStatus());
				return SERVER_FAILURE;
			}
		} catch (Exception e) {
			log.error("Error while fetching cluster info from server.", e);
			return ERROR;
		}
	}

	public ExitCode removePeer(String peerId) {
		try (QDrantHttpClient client = newClient()) {
			GenericBooleanStatusResponse response = client.removePeerFromCluster(peerId, true).sync();
			if (isSuccess(response)) {
				log.info("Peer [{}] removed", peerId);
				return OK;
			} else {
				log.error("Peer removal failed with status [{}]", response.getStatus());
				return SERVER_FAILURE;
			}
		} catch (Exception e) {
			log.error("Error while removing cluster peer with id [{}]", peerId, e);
			return ERROR;
		}
	}

}
