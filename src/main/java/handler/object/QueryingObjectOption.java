package main.java.handler.object;

import main.java.handler.RequestDataOption;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class QueryingObjectOption extends RequestDataOption {

	@Override
	protected JsonObject getOption(HttpServerRequest request, Buffer buffer) {
		return null;
	}

	@Override
	protected String getAddress() {
		// TODO Auto-generated method stub
		return null;
	}

}
