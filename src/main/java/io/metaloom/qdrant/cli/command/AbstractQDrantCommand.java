package io.metaloom.qdrant.cli.command;

import static io.metaloom.qdrant.cli.ExitCode.OK;

import io.metaloom.qdrant.cli.QDrantCLI;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command
public abstract class AbstractQDrantCommand implements QDrantCommand {

	public static final int DEFAULT_BATCH_SIZE = 1000;

	public static final String DEFAULT_BATCH_SIZE_STR = "1000";

	@Spec
	CommandSpec spec;

	@ParentCommand
	private QDrantCLI parent;

	@Override
	public Integer call() {
		spec.commandLine().usage(System.out);
		return OK.code();
	}

	@Override
	public String getHostname() {
		return parent.getHostname();
	}

	@Override
	public int getPort() {
		return parent.getPort();
	}
}
