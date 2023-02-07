package io.metaloom.qdrant.cli.command.action;

import io.metaloom.qdrant.client.http.QDrantHttpClient;

public interface Action {

	/**
	 * Construct a new client to be used. Note that that client must be closed manually or used in an try-with-resource block.
	 * 
	 * @return
	 */
	QDrantHttpClient newClient();

	String getHost();

	int getPort();

}
