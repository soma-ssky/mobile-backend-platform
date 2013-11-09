package main.java.handler.client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import main.java.util.Util;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class BatchHandler implements Handler<HttpServerRequest> {

	@Override
	public void handle(final HttpServerRequest request) {
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				JsonObject data = new JsonObject(buffer.toString());
				JsonArray requests = data.getArray("requests");
				String collection = ((JsonObject) requests.get(0)).getString("path").split("/")[3];
				String method = ((JsonObject) requests.get(0)).getString("method");
				Util.removeInvalidField(data);

				JsonObject option = new JsonObject();
				option.putString("collection", collection);
				switch (method) {
				case "POST":
					option = Util.saveAllObjectOption(collection, saveAllDocuments(requests));
					Util.processObjectRequestViaEB(request, Util.OBJECT_MANAGER_ADDRESS, option, 201);
					break;
				case "DELETE":
					option = Util.deleteAllObjectOption(collection, deleteAllMatcher(requests));
					Util.processObjectRequestViaEB(request, Util.OBJECT_MANAGER_ADDRESS, option, 200);
					break;
				}

			}
		});
	}

	private String saveAllDocuments(JsonArray requestArr) {
		String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
		Iterator<Object> itr = requestArr.iterator();
		JsonArray savedArray = new JsonArray();
		while (itr.hasNext()) {
			JsonObject body = ((JsonObject) itr.next()).getObject("body");
			body.putString("createdAt", currentTime);
			body.putString("updatedAt", currentTime);
			savedArray.addObject(body);
		}

		return savedArray.encode();
	}

	public JsonObject deleteAllMatcher(JsonArray requestArr) {
		JsonArray orMatcher = new JsonArray();
		Iterator<Object> itr = requestArr.iterator();
		while (itr.hasNext()) {
			orMatcher.add(new JsonObject().putString("_id", ((JsonObject) itr.next()).getString("path").split("/")[4]));
		}

		return new JsonObject().putArray("$or", orMatcher);
	}

}
