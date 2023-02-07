package io.metaloom.qdrant.cli.command.action;

import static io.metaloom.qdrant.cli.ExitCode.CONNECT_ERROR;
import static io.metaloom.qdrant.cli.ExitCode.ERROR;

import java.net.ConnectException;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.qdrant.cli.ExitCode;
import io.metaloom.qdrant.cli.command.QDrantCommand;
import io.metaloom.qdrant.client.http.QDrantHttpClient;
import io.metaloom.qdrant.client.http.model.AbstractResponse;

public abstract class AbstractAction implements Action {

	public static final Logger log = LoggerFactory.getLogger(AbstractAction.class);

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

	protected ExitCode checkError(Exception e) {
		Throwable cause = e.getCause();
		if (cause == null) {
			if (log.isDebugEnabled()) {
				log.error("Error", e);
			}
			log.error("Error {}", e.getMessage());
			return ERROR;
		}
		if (cause instanceof SocketException) {
			log.error("Could not connect to server: {}", cause.getMessage());
			return CONNECT_ERROR;
		}

		if (cause instanceof ConnectException) {
			log.error("Error while connecting to server: {}", cause.getMessage());
			return CONNECT_ERROR;
		}
		log.error("Error", e);
		return ERROR;
	}

	public ExitCode withClient(ClientAction clientAction) {
		try (QDrantHttpClient client = newClient()) {
			return clientAction.accept(client);
		} catch (Exception e) {
			return checkError(e);
		}
	}

}
