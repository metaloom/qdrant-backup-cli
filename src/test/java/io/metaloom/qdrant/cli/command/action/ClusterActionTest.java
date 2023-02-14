package io.metaloom.qdrant.cli.command.action;

import static io.metaloom.qdrant.cli.ExitCode.OK;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import io.metaloom.qdrant.cli.AbstractCLITest;
import io.metaloom.qdrant.cli.command.action.impl.ClusterAction;

public class ClusterActionTest extends AbstractCLITest {

	@Test
	public void testClusterInfo() throws IOException {
		String stdout = captureStdOut(() -> {
			assertEquals(OK, new ClusterAction(dummyCommand()).info());
		});
		assertEquals("Cluster Status: disabled\n", stdout);
	}

	@Test
	@Ignore("Clustering not yet tested")
	public void testRemovePeer() {
		String peerId = null;
		assertEquals(OK, new ClusterAction(dummyCommand()).removePeer(peerId));
	}

}
