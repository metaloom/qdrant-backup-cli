package io.metaloom.qdrant.cli.command;

import picocli.CommandLine.Command;

@Command(name = "admin", aliases = { "a" }, description = "Administrative commands. (e.g. lock, unlock)")
public class AdminCommand extends AbstractQDrantCommand {

	@Command(name = "lock")
	public int lock() {
		try {
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 10;
		}
	}

	@Command(name = "unlock")
	public int unlock() {
		try {
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 10;
		}

	}
}
