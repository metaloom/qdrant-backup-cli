package io.metaloom.qdrant.cli.command.action;

import static io.metaloom.qdrant.cli.ExitCode.OK;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import io.metaloom.qdrant.cli.AbstractCLITest;
import io.metaloom.qdrant.cli.command.action.impl.SnapshotAction;
import io.metaloom.qdrant.client.http.impl.HttpErrorException;

public class SnapshotActionTest extends AbstractCLITest {

	@Test
	public void testSnapshot() throws IOException {
		SnapshotAction action = new SnapshotAction(dummyCommand());

		// 1. Assert no snapshots
		String out1 = captureStdOut(() -> {
			assertEquals(OK, action.list(TEST_COLLECTION_NAME));
		});
		assertEquals("No snapshots found\n", out1);

		// 2. Create snapshot
		String out2 = captureStdOut(() -> {
			assertEquals(OK, action.snapshot(TEST_COLLECTION_NAME));
		});
		assertEquals("""
				Creating snapshot for collection [test-collection]
				Snapshot for collection test-collection successfully created.
				""", out2);

		// 3. Assert snapshot listed
		String out3 = captureStdOut(() -> {
			assertEquals(OK, action.list(TEST_COLLECTION_NAME));
		});
		String snapshotName = out3.split("\n")[1].split("\t")[0];

		// 4. Assert recovery
		String location = "file:///qdrant/snapshots/" + TEST_COLLECTION_NAME + "/" + snapshotName;
		String out4 = captureStdOut(() -> {
			assertEquals("Failed to recover snapshot using location " + location, OK,
					action.recover(TEST_COLLECTION_NAME, location));
		});
		String expected = "Recover from snapshot [" + location + "] to collection [" + TEST_COLLECTION_NAME + "]\n"
				+ "Recovery completed without errors.\n";

		assertEquals(expected, out4);
	}

	@Test
	public void testSnapshotWildcard() throws HttpErrorException {
		createCollections();
		assertEquals(OK, new SnapshotAction(dummyCommand()).snapshot("*"));
	}

}
