package main.java.handler.user;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import main.java.handler.RequestDataOption;
import main.java.util.Util;

public class UserQueryOption extends RequestDataOption {

	@Override
	protected JsonObject getOption(HttpServerRequest request, Buffer buffer) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getAddress() {
		return Util.AUTH_MANAGER_ADDRESS;
	}

}
