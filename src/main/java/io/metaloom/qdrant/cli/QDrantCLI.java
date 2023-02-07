package io.metaloom.qdrant.cli;

import ch.qos.logback.classic.Level;
import io.metaloom.qdrant.cli.command.impl.AdminCommand;
import io.metaloom.qdrant.cli.command.impl.ClusterCommand;
import io.metaloom.qdrant.cli.command.impl.CollectionCommand;
import io.metaloom.qdrant.cli.command.impl.PointCommand;
import io.metaloom.qdrant.cli.command.impl.SnapshotCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

@Command(name = "qdrant-backup-cli", mixinStandardHelpOptions = false, version = "qdrant-backup-cli 0.9.0", description = "CLI tool for the qdrant vector database", showDefaultValues = true, subcommands = {
	SnapshotCommand.class,
	CollectionCommand.class,
	ClusterCommand.class,
	PointCommand.class,
	AdminCommand.class
})
public class QDrantCLI implements Runnable {

	private static QDrantCLI instance = new QDrantCLI();

	public static final int DEFAULT_PORT = 6333;
	public static final String DEFAULT_PORT_STR = "6333";
	public static final String DEFAULT_HOSTNAME = "localhost";

	private String hostname = DEFAULT_HOSTNAME;

	private int port = DEFAULT_PORT;

	@Spec
	CommandSpec spec;

	@Option(names = "-v", scope = ScopeType.INHERIT)
	public void setVerbose(boolean[] verbose) {
		Level level = Level.INFO;
		if (verbose.length > 0) {
			level = Level.DEBUG;
		}
		if (verbose.length >= 1) {
			level = Level.TRACE;
		}
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
			.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);
	}

	public String getHostname() {
		return hostname;
	}

	@Option(names = { "-h", "--hostname" }, description = "Hostname to connect to", defaultValue = DEFAULT_HOSTNAME, scope = ScopeType.INHERIT)
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	@Option(names = { "-p", "--port" }, description = "HTTP port to connect to.", defaultValue = DEFAULT_PORT_STR, scope = ScopeType.INHERIT)
	public void setPort(int port) {
		this.port = port;
	}

	public static void main(String... args) {
		System.exit(execute(args));
	}

	public static int execute(String[] args) {
		CommandLine cmd = new CommandLine(QDrantCLI.instance());
		return cmd.execute(args);
	}

	@Override
	public void run() {
		spec.commandLine().usage(System.out);
	}

	public static QDrantCLI instance() {
		return instance;
	}
}
