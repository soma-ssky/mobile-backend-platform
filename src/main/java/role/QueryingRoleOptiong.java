package main.java.role;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import main.java.handler.RequestDataOption;
import main.java.util.Util;

public class QueryingRoleOptiong extends RequestDataOption {

	@Override
	protected JsonObject getOption(HttpServerRequest request, Buffer buffer) {
		JsonObject option = new JsonObject();
		String objectId = request.path().split("/")[3];
		option.putString("action", "delete");
		option.putObject("document", new JsonObject().putString("objectId", objectId));
		return option;
	}

	@Override
	protected String getAddress() {
		return Util.ROLE_MANAGER_ADDRESS;
	}

}
