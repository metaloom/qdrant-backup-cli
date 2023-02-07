package io.metaloom.qdrant.cli.command.impl;

import static io.metaloom.qdrant.cli.ExitCode.ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.qdrant.cli.command.AbstractQDrantCommand;
import io.metaloom.qdrant.cli.command.action.impl.AdminAction;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "admin", aliases = { "a" }, description = "Administrative commands. (e.g. lock, unlock)")
public class AdminCommand extends AbstractQDrantCommand {

	public static final Logger log = LoggerFactory.getLogger(AdminCommand.class);

	@Command(name = "lock", description = "Enable the write lock and set the provided msg.")
	public int lock(@Parameters(index = "0", description = "Lock message which will be set.") String msg) {
		try {
			return new AdminAction(this).lock(msg).code();
		} catch (Exception e) {
			log.error("Locking the server failed", e);
			return ERROR.code();
		}
	}

	@Command(name = "status", description = "Return the current lock status.")
	public int status() {
		try {
			return new AdminAction(this).status().code();
		} catch (Exception e) {
			log.error("Fetching lock status failed", e);
			return ERROR.code();
		}
	}

	@Command(name = "unlock", description = "Disable the write lock on the server.")
	public int unlock() {
		try {
			return new AdminAction(this).unlock().code();
		} catch (Exception e) {
			log.error("Unlocking the server failed", e);
			return ERROR.code();
		}

	}
}
