package main.java.me.ssky.users;

import java.util.Map;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.Util;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class SigningUpUserOption extends EventBusOption {
	private String method;

	@Override
	public String address() {
		return Util.AUTH_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		method = data.getString("_method");
		Util.removeInvalidField(data);

		JsonObject option = new JsonObject();
		if (method == null) {
			option.putString("action", "signup");
			option.putObject("document", data);
		} else if (method.equals("GET")) {
			option = new RetrievingUserOption().option(request, data);
		} else {
			// bad request
		}
		return option;
	}

	@Override
	public Map<String, String> headers(JsonObject result) {
		if (method == null) {
			String location = "/" + Util.VERSION + "/users/" + result.getString("objectId");
			return Util.getCreatedResponseHeaders(statusCodeInSuccess(), result.encode().length(), location);
		} else {
			return Util.getResponseHeaders(statusCodeInSuccess(), result.encode().length());
		}
	}

	@Override
	public int statusCodeInSuccess() {
		return (method == null ? 201 : 200);
	}
	
	
}
