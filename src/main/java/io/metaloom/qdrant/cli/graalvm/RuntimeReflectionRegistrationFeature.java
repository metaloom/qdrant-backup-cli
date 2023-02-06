package io.metaloom.qdrant.cli.graalvm;

import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import io.metaloom.qdrant.client.http.model.collection.filter.Filter;
import io.metaloom.qdrant.client.http.model.point.Payload;
import io.metaloom.qdrant.client.http.model.point.PointsScrollRequest;
import io.metaloom.qdrant.client.http.model.point.PointsScrollResponse;
import io.metaloom.qdrant.client.http.model.point.Record;
import io.metaloom.qdrant.client.http.model.point.ScrollResult;

class RuntimeReflectionRegistrationFeature implements Feature {
	public void beforeAnalysis(BeforeAnalysisAccess access) {

		// Jackson
		System.out.println(" - Registering jackson classes");
		RuntimeReflection.register(com.fasterxml.jackson.databind.deser.Deserializers.class);
		RuntimeReflection.register(com.fasterxml.jackson.databind.ser.Serializers.class);
		RuntimeReflection.register(com.fasterxml.jackson.databind.ext.Java7HandlersImpl.class);
		RuntimeReflection.register(com.fasterxml.jackson.databind.ext.Java7SupportImpl.class);

		// Models
		System.out.println(" - Registering qdrant-java-client model classes");
		registerModelClass(ScrollResult.class);
		registerModelClass(Record.class);
		registerModelClass(Filter.class);
		registerModelClass(Payload.class);
		registerModelClass(PointsScrollRequest.class);
		registerModelClass(PointsScrollResponse.class);
	}

	private void registerModelClass(Class<?> clazz) {
		RuntimeReflection.register(clazz);
		RuntimeReflection.register(clazz.getDeclaredFields());
		RuntimeReflection.registerForReflectiveInstantiation(clazz);
		RuntimeReflection.registerAsQueried(clazz.getMethods());
		RuntimeReflection.registerAsQueried(clazz.getConstructors());
		RuntimeReflection.register(clazz.getConstructors());
		RuntimeReflection.register(clazz.getMethods());
	}
}
