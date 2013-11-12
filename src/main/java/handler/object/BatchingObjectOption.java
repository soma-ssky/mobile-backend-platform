package main.java.handler.object;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import main.java.handler.RequestDataOption;
import main.java.util.Util;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class BatchingObjectOption extends RequestDataOption {

	@Override
	protected JsonObject getOption(HttpServerRequest request, Buffer buffer) {
		JsonObject data = new JsonObject(buffer.toString());
		JsonArray requests = data.getArray("requests");
		String collection = ((JsonObject) requests.get(0)).getString("path").split("/")[3];
		String method = ((JsonObject) requests.get(0)).getString("method");
		Util.removeInvalidField(data);

		JsonObject option = new JsonObject();
		option.putString("collection", collection);
		switch (method) {
		case "POST":
			option.putString("action", "saveall");
			option.putString("documents", saveAllDocuments(requests));
			break;
		case "DELETE":
			option.putString("action", "deleteall");
			option.putObject("matcher", deleteAllMatcher(requests));
			break;
		}

		return option;

	}

	@Override
	protected String getAddress() {
		return Util.OBJECT_MANAGER_ADDRESS;
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
