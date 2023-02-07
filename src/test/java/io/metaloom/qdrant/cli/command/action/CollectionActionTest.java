package io.metaloom.qdrant.cli.command.action;

import static io.metaloom.qdrant.cli.ExitCode.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import io.metaloom.qdrant.cli.AbstractCLITest;
import io.metaloom.qdrant.cli.command.action.impl.CollectionAction;
import io.metaloom.qdrant.client.http.impl.HttpErrorException;

public class CollectionActionTest extends AbstractCLITest {

	@Test
	public void testBackup() throws IOException, HttpErrorException {
		createCollections();
		// Test backup to stdout
		String out = captureStdOut(() -> {
			assertEquals(OK, new CollectionAction(dummyCommand()).backup(1, "-"));
		});
		assertTrue(out.split("\n").length >= 20);

		// Test backup to file
		File tmpFile = prepareTestFile("collections-dump-test");
		String out2 = captureStdOut(() -> {
			assertEquals(OK, new CollectionAction(dummyCommand()).backup(1, tmpFile.getAbsolutePath()));
		});
		assertEquals("", out2);
		assertTrue(tmpFile.exists());

	}

}
