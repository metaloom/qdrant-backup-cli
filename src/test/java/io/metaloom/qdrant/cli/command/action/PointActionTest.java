package io.metaloom.qdrant.cli.command.action;

import static io.metaloom.qdrant.cli.ExitCode.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import io.metaloom.qdrant.cli.AbstractCLITest;
import io.metaloom.qdrant.cli.command.action.impl.PointAction;

public class PointActionTest extends AbstractCLITest {

	@Test
	public void testDumpPoints() throws IOException {
		String out = captureStdOut(() -> {
			assertEquals(OK, new PointAction(dummyCommand()).backup(5, TEST_COLLECTION_NAME, "-"));
		});
		assertTrue(out.split("\n").length >= 500);

		// Test backup to file
		File tmpFile = prepareTestFile("point-dump-test");
		String out2 = captureStdOut(() -> {
			assertEquals(OK, new PointAction(dummyCommand()).backup(5, TEST_COLLECTION_NAME, tmpFile.getAbsolutePath()));
		});
		assertEquals("", out2);
		assertTrue(tmpFile.exists());

	}

	@Test
	public void testCountPoints() throws IOException {
		String out = captureStdOut(() -> {
			assertEquals(OK, new PointAction(dummyCommand()).count(TEST_COLLECTION_NAME, true));
		});
		assertEquals("Points: 500\n", out);
	}
}
