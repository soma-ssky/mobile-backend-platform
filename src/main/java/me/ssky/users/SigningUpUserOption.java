package main.java.me.ssky.users;

import java.util.Map;

import main.java.me.ssky.util.EventBusOption;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class SigningUpUserOption extends EventBusOption {
	private String method;

	@Override
	public String address() {
		return ServerUtils.AUTH_MANAGER_ADDRESS;
	}

	@Override
	public JsonObject option(HttpServerRequest request, JsonObject data) {
		method = data.getString("_method");
		ServerUtils.removeInvalidField(data);

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
			String location = "/" + ServerUtils.VERSION + "/users/" + result.getString("objectId");
			return ServerUtils.responseHeadersInCreated(statusCodeInSuccess(), result.encode().length(), location);
		} else {
			return ServerUtils.responseHeaders(statusCodeInSuccess());
		}
	}

	@Override
	public int statusCodeInSuccess() {
		return (method == null ? 201 : 200);
	}
	
	
}
