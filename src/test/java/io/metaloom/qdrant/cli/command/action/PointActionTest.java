package io.metaloom.qdrant.cli.command.action;

import static io.metaloom.qdrant.cli.ExitCode.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import io.metaloom.qdrant.cli.AbstractCLITest;
import io.metaloom.qdrant.cli.command.action.impl.PointAction;
import io.metaloom.qdrant.client.http.QDrantHttpClient;
import io.metaloom.qdrant.client.http.impl.HttpErrorException;
import io.metaloom.qdrant.client.http.model.collection.CollectionCreateRequest;
import io.metaloom.qdrant.client.http.model.collection.config.Distance;
import io.metaloom.qdrant.client.http.model.point.PointCountRequest;

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
	public void testRestorePoints() throws HttpErrorException, IOException {
		File tmpFile = prepareTestFile("point-dump-test-2");
		assertEquals(OK, new PointAction(dummyCommand()).backup(5, TEST_COLLECTION_NAME, tmpFile.getAbsolutePath()));

		// Create second collection for restore purpose
		final String TEST_RESTORE_COLLECTION_NAME = TEST_COLLECTION_NAME + "_2";
		try (QDrantHttpClient client = newClient()) {
			CollectionCreateRequest request = new CollectionCreateRequest();
			request.setVectors(4, Distance.EUCLID);
			client.createCollection(TEST_RESTORE_COLLECTION_NAME, request).sync();
		}

		assertEquals(OK, new PointAction(dummyCommand()).restore(5, TEST_RESTORE_COLLECTION_NAME, tmpFile.getAbsolutePath()));

		try (QDrantHttpClient client = newClient()) {
			PointCountRequest request = new PointCountRequest().setExact(true);
			long count = client.countPoints(TEST_RESTORE_COLLECTION_NAME, request).sync().getResult().getCount();
			assertEquals("The new collection should now have points in it.", TEST_SIZE, count);
		}
	}

	@Test
	public void testCountPoints() throws IOException {
		String out = captureStdOut(() -> {
			assertEquals(OK, new PointAction(dummyCommand()).count(TEST_COLLECTION_NAME, true));
		});
		assertEquals("Points: 500\n", out);
	}
}
