package io.metaloom.qdrant.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;

import org.junit.Before;
import org.junit.Rule;

import ch.qos.logback.classic.Level;
import io.metaloom.qdrant.cli.command.AbstractQDrantCommand;
import io.metaloom.qdrant.cli.command.QDrantCommand;
import io.metaloom.qdrant.client.http.QDrantHttpClient;
import io.metaloom.qdrant.client.http.impl.HttpErrorException;
import io.metaloom.qdrant.client.http.model.collection.CollectionCreateRequest;
import io.metaloom.qdrant.client.http.model.collection.config.Distance;
import io.metaloom.qdrant.client.http.model.point.PointStruct;
import io.metaloom.qdrant.client.http.model.point.PointsListUpsertRequest;
import io.metaloom.qdrant.container.MuteableQDrantContainer;

public abstract class AbstractCLITest {

	@Rule
	public MuteableQDrantContainer qdrant = new MuteableQDrantContainer();

	protected void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static final String TEST_COLLECTION_NAME = "test-collection";
	public static final int TEST_SIZE = 500;

	@Before
	public void setupTestData() throws HttpErrorException {
		try (QDrantHttpClient client = newClient()) {
			CollectionCreateRequest request = new CollectionCreateRequest();
			request.setVectors(4, Distance.EUCLID);
			client.createCollection(TEST_COLLECTION_NAME, request).sync();

			PointsListUpsertRequest pointRequest = new PointsListUpsertRequest();
			for (int i = 0; i < TEST_SIZE; i++) {
				pointRequest.addPoint(PointStruct.of(0.42f, 0.43f, 0.44f, 0.45f).setId(i));
			}
			client.upsertPoints(TEST_COLLECTION_NAME, pointRequest, true).sync();
		}
	}

	public QDrantHttpClient newClient() {
		return QDrantHttpClient.builder().setPort(qdrant.httpPort()).build();
	}

	public QDrantCommand dummyCommand() {
		return new AbstractQDrantCommand() {

			@Override
			public String getHostname() {
				return qdrant.getHost();
			}

			@Override
			public int getPort() {
				return qdrant.httpPort();
			}
		};
	}

	protected String captureStdOut(Runnable command) throws IOException {
		setLoggingLevel(Level.ERROR);
		qdrant.mute();
		PrintStream out = System.out;
		try {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				System.setOut(new PrintStream(baos));
				command.run();
				String stdout = new String(baos.toByteArray(), Charset.defaultCharset());
				out.println("Captured:\n" + stdout);
				return stdout;
			}
		} finally {
			System.setOut(out);
			qdrant.unmute();
			setLoggingLevel(Level.INFO);
		}
	}

	public static void setLoggingLevel(Level level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
			.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);
	}

	public void createCollections() throws HttpErrorException {
		try (QDrantHttpClient client = newClient()) {
			for (int i = 0; i < 20; i++) {
				CollectionCreateRequest request = new CollectionCreateRequest();
				request.setVectors(4, Distance.EUCLID);
				client.createCollection(TEST_COLLECTION_NAME + "_" + i, request).sync();
			}
		}

	}

	public File prepareTestFile(String name) {
		File tmpFile = new File("target/" + name);
		if (tmpFile.exists()) {
			tmpFile.delete();
		}
		assertFalse(tmpFile.exists());
		return tmpFile;
	}

	public void assertEmpty(String msg, String text) {
		assertEquals(msg, 0, text.length());
	}

}
