package io.metaloom.qdrant.cli.command.action;

import io.metaloom.qdrant.cli.command.QDrantCommand;
import io.metaloom.qdrant.client.http.QDrantHttpClient;
import io.metaloom.qdrant.client.http.model.AbstractResponse;

public abstract class AbstractAction implements Action {

	protected final String host;
	protected final int port;

	public AbstractAction(QDrantCommand command) {
		this.host = command.getHostname();
		this.port = command.getPort();
	}

	@Override
	public QDrantHttpClient newClient() {
		return QDrantHttpClient.builder().setHostname(host).setPort(port).build();
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public int getPort() {
		return port;
	}

	protected boolean isSuccess(AbstractResponse response) {
		return "ok".equalsIgnoreCase(response.getStatus());
	}
}
