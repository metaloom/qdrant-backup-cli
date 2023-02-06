package io.metaloom.qdrant.cli.command;

import java.util.concurrent.Callable;

import io.metaloom.qdrant.cli.QDrantCLI;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command
public abstract class AbstractQDrantCommand implements Callable<Integer> {

	public static final int DEFAULT_BATCH_SIZE = 1000;

	public static final String DEFAULT_BATCH_SIZE_STR = "1000";

	@Spec
	CommandSpec spec;

	@ParentCommand
	private QDrantCLI parent;

	@Override
	public Integer call() {
		spec.commandLine().usage(System.out);
		return 0;
	}

	public String getHostname() {
		return parent.getHostname();
	}

	public int getPort() {
		return parent.getPort();
	}
}
