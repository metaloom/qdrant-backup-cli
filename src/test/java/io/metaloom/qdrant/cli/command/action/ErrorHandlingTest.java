package io.metaloom.qdrant.cli.command.action;

import static io.metaloom.qdrant.cli.ExitCode.CONNECT_ERROR;
import static io.metaloom.qdrant.cli.ExitCode.ERROR;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import io.metaloom.qdrant.cli.AbstractCLITest;
import io.metaloom.qdrant.cli.command.action.impl.SnapshotAction;

public class ErrorHandlingTest extends AbstractCLITest {

	@Test
	public void testConnectionError() throws IOException {
		String out1 = captureStdOut(() -> {
			assertEquals(ERROR, new SnapshotAction(bogusCommand(-10)).list("bogus"));
		});
		assertEquals("Error unexpected port: -10\n", out1);
	}

	@Test
	public void testConnectionError2() throws IOException {
		String out1 = captureStdOut(() -> {
			assertEquals(CONNECT_ERROR, new SnapshotAction(bogusCommand(22)).list("bogus"));
		});
		assertEquals("Could not connect to server: Network is unreachable\n", out1);
	}
}
