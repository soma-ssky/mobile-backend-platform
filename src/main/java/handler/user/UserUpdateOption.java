package main.java.handler.user;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import main.java.handler.RequestDataOption;
import main.java.util.Util;

public class UserUpdateOption extends RequestDataOption {

	@Override
	protected JsonObject getOption(HttpServerRequest request, Buffer buffer) {
		JsonObject option = new JsonObject();
		String objectId = request.path().split("/")[3];
		String sessionToken = request.params().get("sessionToken");
		option.putString("action", "update");
		JsonObject document = new JsonObject(buffer.toString());
		document.putString("sessionToken", sessionToken);
		document.putString("objectId", objectId);
		option.putObject("document", document);

		return option;
	}

	@Override
	protected String getAddress() {
		return Util.AUTH_MANAGER_ADDRESS;
	}

}
