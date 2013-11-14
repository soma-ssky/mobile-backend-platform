package main.java.me.ssky.users;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.Util;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class DeletingUserOption extends EventBusOption {

	@Override
	public String address() {
		return Util.AUTH_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		JsonObject document = data;
		document.putString("objectId", request.path().split("/")[3]);
		document.putString("sessionToken", Util.convertHeadersToJsonObject(request.headers()).getString("X-Parse-Session-Token"));

		JsonObject option = new JsonObject();
		option.putString("action", "delete");
		option.putObject("document", document);
		return option;
	}

}
