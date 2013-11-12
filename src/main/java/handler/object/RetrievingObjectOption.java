package main.java.handler.object;

import main.java.handler.RequestDataOption;
import main.java.util.Util;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class RetrievingObjectOption extends RequestDataOption {

	@Override
	protected JsonObject getOption(HttpServerRequest request, Buffer buffer) {
		JsonObject option = new JsonObject();
		String collection = request.path().split("/")[3];
		String objectId = request.path().split("/")[4];
		option.putString("action", "fetch");
		option.putString("collection", collection);
		option.putObject("matcher", new JsonObject().putString("_id", objectId));

		return option;
	}

	@Override
	protected String getAddress() {
		return Util.OBJECT_MANAGER_ADDRESS;
	}

}
