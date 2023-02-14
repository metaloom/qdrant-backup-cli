package io.metaloom.qdrant.cli.graalvm;

import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.pattern.LineSeparatorConverter;
import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.core.ConsoleAppender;
import io.metaloom.qdrant.client.http.model.cluster.ClusterInfoResponse;
import io.metaloom.qdrant.client.http.model.cluster.ClusterStatus;
import io.metaloom.qdrant.client.http.model.cluster.ClusterStatusResponse;
import io.metaloom.qdrant.client.http.model.collection.CollectionDescription;
import io.metaloom.qdrant.client.http.model.collection.CollectionInfo;
import io.metaloom.qdrant.client.http.model.collection.CollectionListResponse;
import io.metaloom.qdrant.client.http.model.collection.CollectionResponse;
import io.metaloom.qdrant.client.http.model.collection.CollectionsResponse;
import io.metaloom.qdrant.client.http.model.collection.PayloadIndexInfo;
import io.metaloom.qdrant.client.http.model.collection.filter.Filter;
import io.metaloom.qdrant.client.http.model.point.CountResult;
import io.metaloom.qdrant.client.http.model.point.Payload;
import io.metaloom.qdrant.client.http.model.point.PointCountRequest;
import io.metaloom.qdrant.client.http.model.point.PointCountResponse;
import io.metaloom.qdrant.client.http.model.point.PointsScrollRequest;
import io.metaloom.qdrant.client.http.model.point.PointsScrollResponse;
import io.metaloom.qdrant.client.http.model.point.Record;
import io.metaloom.qdrant.client.http.model.point.ScrollResult;
import io.metaloom.qdrant.client.http.model.service.LockOption;
import io.metaloom.qdrant.client.http.model.service.LockOptionResponse;
import io.metaloom.qdrant.client.http.model.service.LockRequest;
import io.metaloom.qdrant.client.http.model.snapshot.SnapshotDescription;
import io.metaloom.qdrant.client.http.model.snapshot.SnapshotListResponse;
import io.metaloom.qdrant.client.http.model.snapshot.SnapshotRecoverRequest;
import io.metaloom.qdrant.client.http.model.snapshot.SnapshotResponse;

class RuntimeReflectionRegistrationFeature implements Feature {
	public void beforeAnalysis(BeforeAnalysisAccess access) {

		System.out.println(" - Registering jackson classes");
		RuntimeReflection.register(com.fasterxml.jackson.databind.deser.Deserializers.class);
		RuntimeReflection.register(com.fasterxml.jackson.databind.ser.Serializers.class);
		RuntimeReflection.register(com.fasterxml.jackson.databind.ext.Java7HandlersImpl.class);
		RuntimeReflection.register(com.fasterxml.jackson.databind.ext.Java7SupportImpl.class);

		System.out.println(" - Registering okhttp classes");
		RuntimeReflection.register(kotlin.internal.jdk8.JDK8PlatformImplementations.class);
		RuntimeReflection.register(kotlin.internal.jdk7.JDK7PlatformImplementations.class);

		System.out.println(" - Registering qdrant-java-client model classes");

		// Points
		registerFully(ScrollResult.class);
		registerFully(Record.class);
		registerFully(Filter.class);
		registerFully(Payload.class);
		registerFully(PointsScrollRequest.class);
		registerFully(PointsScrollResponse.class);

		// Count
		registerFully(PointCountRequest.class);
		registerFully(PointCountResponse.class);
		registerFully(CountResult.class);

		// Collections
		registerFully(CollectionListResponse.class);
		registerFully(CollectionsResponse.class);
		registerFully(CollectionDescription.class);
		registerFully(CollectionResponse.class);
		registerFully(CollectionInfo.class);
		registerFully(PayloadIndexInfo.class);

		// Cluster / Locking
		registerFully(ClusterStatusResponse.class);
		registerFully(ClusterInfoResponse.class);
		registerFully(LockOptionResponse.class);
		registerFully(ClusterStatus.class);
		registerFully(LockOption.class);
		registerFully(LockRequest.class);

		// Snapshots
		registerFully(SnapshotListResponse.class);
		registerFully(SnapshotRecoverRequest.class);
		registerFully(SnapshotResponse.class);
		registerFully(SnapshotDescription.class);

		System.out.println(" - Registering logback classes");
		registerFully(ConsoleAppender.class);
		registerFully(PatternLayoutEncoder.class);
		registerFully(LineSeparatorConverter.class);
		registerFully(MessageConverter.class);

	}

	private void registerFully(Class<?> clazz) {
		RuntimeReflection.register(clazz);
		RuntimeReflection.register(clazz.getDeclaredFields());
		RuntimeReflection.registerForReflectiveInstantiation(clazz);
		RuntimeReflection.registerAsQueried(clazz.getMethods());
		RuntimeReflection.registerAsQueried(clazz.getConstructors());
		RuntimeReflection.register(clazz.getConstructors());
		RuntimeReflection.register(clazz.getMethods());
	}
}
