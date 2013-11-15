package main.java.me.ssky.users;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class UpdatingUserOption extends EventBusOption {

	@Override
	public String address() {
		return ServerUtils.AUTH_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		ServerUtils.removeInvalidField(data);

		JsonObject document = data;
		document.putString("objectId", request.path().split("/")[3]);
		document.putString("sessionToken", ServerUtils.convertHeadersToJsonObject(request.headers()).getString("X-Parse-Session-Token"));

		JsonObject option = new JsonObject();
		option.putString("action", "update");
		option.putObject("document", document);
		return option;
	}

}
