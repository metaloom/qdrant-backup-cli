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
import io.metaloom.qdrant.client.http.model.service.LockOptionResponse;

public class AdminAction extends AbstractAction {

	public static final Logger log = LoggerFactory.getLogger(AdminAction.class);

	public AdminAction(QDrantCommand command) {
		super(command);
	}

	public ExitCode lock(String msg) {
		return updateLockOptions(msg, true);
	}

	public ExitCode unlock() {
		return updateLockOptions(null, false);
	}

	public ExitCode status() {
		try (QDrantHttpClient client = newClient()) {
			LockOptionResponse response = client.getLockOptions().sync();
			if (isSuccess(response)) {
				String msg = response.getResult().getErrorMessage();
				String lock = response.getResult().isWrite() ? "LOCKED" : "UNLOCKED";
				System.out.println("[MSG]\t[Lock]");
				System.out.println(msg + "\t" + lock);
				return OK;
			} else {
				log.info("Loading lock options failed with status [{}]", response.getStatus());
				return SERVER_FAILURE;
			}
		} catch (Exception e) {
			log.error("Error while fetching lock options.", e);
			return ERROR;
		}
	}

	private ExitCode updateLockOptions(String msg, boolean write) {
		try (QDrantHttpClient client = newClient()) {
			LockOptionResponse response = client.setLockOptions(msg, write).sync();
			if (isSuccess(response)) {
				log.info("Lock options updated");
				return OK;
			} else {
				log.info("Updating lock options failed with status [{}]", response.getStatus());
				return SERVER_FAILURE;
			}
		} catch (Exception e) {
			log.error("Error while updating lock options.", e);
			return ERROR;
		}
	}

}
