package io.metaloom.qdrant.cli.command.action.impl;

import static io.metaloom.qdrant.cli.ExitCode.OK;
import static io.metaloom.qdrant.cli.ExitCode.SERVER_FAILURE;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.qdrant.cli.ExitCode;
import io.metaloom.qdrant.cli.command.QDrantCommand;
import io.metaloom.qdrant.cli.command.action.AbstractAction;
import io.metaloom.qdrant.client.http.model.GenericBooleanStatusResponse;
import io.metaloom.qdrant.client.http.model.collection.CollectionDescription;
import io.metaloom.qdrant.client.http.model.collection.CollectionListResponse;
import io.metaloom.qdrant.client.http.model.snapshot.SnapshotDescription;
import io.metaloom.qdrant.client.http.model.snapshot.SnapshotListResponse;
import io.metaloom.qdrant.client.http.model.snapshot.SnapshotRecoverRequest;
import io.metaloom.qdrant.client.http.model.snapshot.SnapshotResponse;

public class SnapshotAction extends AbstractAction {

	public static final Logger log = LoggerFactory.getLogger(SnapshotAction.class);

	public SnapshotAction(QDrantCommand command) {
		super(command);
	}

	public ExitCode recover(String collectionName, String snapshotLocation) {
		return withClient(client -> {
			SnapshotRecoverRequest request = new SnapshotRecoverRequest();
			request.setLocation(snapshotLocation);
			log.info("Recover from snapshot [{}] to collection [{}]", snapshotLocation, collectionName);
			GenericBooleanStatusResponse response = client.recoverSnapshot(collectionName, request).sync();
			if (isSuccess(response)) {
				log.info("Recovery completed without errors.");
				return OK;
			} else {
				log.error("Recovery of snapshot {} for collection {} failed with status {}", snapshotLocation,
						collectionName, response.getStatus());
				return SERVER_FAILURE;
			}
		});
	}

	public ExitCode snapshot(String collectionNames) {
		return withClient(client -> {
			List<String> collections = Arrays.asList(collectionNames.split(","));
			if ("*".equalsIgnoreCase(collectionNames)) {
				CollectionListResponse response = client.listCollections().sync();
				if (isSuccess(response)) {
					collections = response.getResult().getCollections().stream().map(CollectionDescription::getName)
							.collect(Collectors.toList());
					if (log.isDebugEnabled()) {
						log.debug("Loaded list of {} from server.", collections.size());
					}
				} else {
					log.error("Error while loading list of all collections [{}]", response.getStatus());
					return SERVER_FAILURE;
				}
			}

			for (String collectionName : collections) {
				log.info("Creating snapshot for collection [{}]", collectionName);
				SnapshotResponse response = client.createCollectionSnapshot(collectionName).sync();
				if (isSuccess(response)) {
					log.info("Snapshot for collection {} successfully created.", collectionName);
				} else {
					log.info("Snapshot creation for {} failed with status {}.", collectionName, response.getStatus());
					return SERVER_FAILURE;
				}
			}
			return OK;
		});
	}

	public ExitCode list(String collectionName) {
		return withClient(client -> {
			SnapshotListResponse response = client.listCollectionSnapshots(collectionName).sync();
			if (isSuccess(response)) {
				List<SnapshotDescription> snapshots = response.getResult();
				if (snapshots.isEmpty()) {
					log.info("No snapshots found");
					return OK;
				} else {
					log.info("[Name]\t[Creation Time]\t[Size]");
				}
				for (SnapshotDescription snapshot : snapshots) {
					String humanSize = FileUtils.byteCountToDisplaySize(snapshot.getSize());
					log.info(snapshot.getName() + "\t" + snapshot.getCreationTime() + "\t" + humanSize);
				}
				return OK;
			} else {
				log.error("Listing snapshots failed with status [{}]", response.getStatus());
				return SERVER_FAILURE;
			}
		});
	}

}
