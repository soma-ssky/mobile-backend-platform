package main.java.me.ssky.util;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class CreatingRoleOption extends EventBusOption {

	@Override
	public String address() {
		return Util.OBJECT_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		Util.removeInvalidField(data);
		JsonObject option = new JsonObject();
		option.putString("action", "save");
		option.putString("collection", "_Role");
		option.putObject("document", data);
		return option;
	}

}
