package io.metaloom.qdrant.cli.command.action;

import io.metaloom.qdrant.cli.ExitCode;
import io.metaloom.qdrant.client.http.QDrantHttpClient;

@FunctionalInterface
public interface ClientAction {

	ExitCode accept(QDrantHttpClient client) throws Exception;

}
