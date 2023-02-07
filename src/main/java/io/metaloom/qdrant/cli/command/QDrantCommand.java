package io.metaloom.qdrant.cli.command;

import java.util.concurrent.Callable;

public interface QDrantCommand extends Callable<Integer> {

	int getPort();

	String getHostname();

}
