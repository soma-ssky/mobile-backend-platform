package main.java.handler;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

abstract public class RequestDataOption {
	
	abstract protected JsonObject getOption(HttpServerRequest request, Buffer buffer);

	abstract protected String getAddress();

}


