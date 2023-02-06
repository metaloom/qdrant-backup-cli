package io.metaloom.qdrant.cli;

import org.junit.Before;

import io.metaloom.qdrant.client.AbstractContainerTest;
import io.metaloom.qdrant.client.http.QDrantHttpClient;
import io.metaloom.qdrant.client.http.impl.HttpErrorException;
import io.metaloom.qdrant.client.http.model.collection.CollectionCreateRequest;
import io.metaloom.qdrant.client.http.model.collection.config.Distance;
import io.metaloom.qdrant.client.http.model.point.PointStruct;
import io.metaloom.qdrant.client.http.model.point.PointsListUpsertRequest;

public abstract class AbstractCommandTest extends AbstractContainerTest {

	public static final String TEST_COLLECTION_NAME = "test-collection";
	public static final int TEST_SIZE = 500;

	@Before
	public void setupTestData() throws HttpErrorException {
		try (QDrantHttpClient client = QDrantHttpClient.builder().setPort(qdrant.httpPort()).build()) {
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
}
