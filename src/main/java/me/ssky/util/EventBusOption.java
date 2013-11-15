package main.java.me.ssky.util;

import java.util.Map;

import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

abstract public class EventBusOption {

	abstract public String address();

	abstract public JsonObject option(HttpServerRequest request, JsonObject data);

	public Map<String, String> headers(JsonObject result) {
		return ServerUtils.responseHeaders(statusCodeInSuccess());
	}

	public int statusCodeInSuccess() {
		return 200;
	}

}
