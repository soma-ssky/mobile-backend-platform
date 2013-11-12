package main.java.handler.object;

import main.java.handler.RequestDataOption;
import main.java.util.Util;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class ClientObjectOption extends RequestDataOption {

	@Override
	protected JsonObject getOption(HttpServerRequest request, Buffer buffer) {
		JsonObject data = new JsonObject(buffer.toString());
		String method = data.getString("_method");
		Util.removeInvalidField(data);

		String collection = request.path().split("/")[3];
		String objectId = request.path().split("/")[4];

		JsonObject option = new JsonObject();
		option.putString("collection", collection);
		option.putObject("matcher", new JsonObject().putString("_id", objectId));

		switch (method) {
		case "GET":
			option.putString("action", "fetch");
		case "PUT":
			option.putString("action", "update");
			option.putObject("objNew", data);
		case "DELETE":
			option.putString("action", "delete");
		default:
			// invalid request
		}

		return option;
	}

	@Override
	protected String getAddress() {
		return Util.OBJECT_MANAGER_ADDRESS;
	}

}
