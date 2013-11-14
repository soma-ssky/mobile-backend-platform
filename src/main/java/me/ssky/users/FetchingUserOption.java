package main.java.me.ssky.users;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.Util;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class FetchingUserOption extends EventBusOption {

	@Override
	public String address() {
		return Util.AUTH_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		Util.removeInvalidField(data);

		JsonObject option = new JsonObject();
		option.putString("action", "retrieve");
		option.putObject("document", new JsonObject().putString("objectId", request.path().split("/")[3]));
		return option;
	}

}
