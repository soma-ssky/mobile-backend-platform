package main.java.me.ssky.util;

import main.java.me.ssky.server.ServerMain;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonObject;

public class EventBusHandler implements Handler<HttpServerRequest> {
	private EventBusOption ebOption;
	private HttpServerRequest request;

	public EventBusHandler(EventBusOption ebOption) {
		this.ebOption = ebOption;
	}

	@Override
	public void handle(HttpServerRequest request) {
		this.request = request;
		request.bodyHandler(bodyHandler);
	}

	Handler<Buffer> bodyHandler = new Handler<Buffer>() {
		@Override
		public void handle(Buffer buffer) {
			JsonObject data = (buffer.length() > 0) ? new JsonObject(buffer.toString()) : null;
			JsonObject option = ebOption.option(request, data);
			ServerMain._vertx.eventBus().send(ebOption.address(), option, ebHandler);
		}
	};

	Handler<Message<JsonObject>> ebHandler = new Handler<Message<JsonObject>>() {
		@Override
		public void handle(Message<JsonObject> message) {
			HttpServerResponse response = request.response();
			int code = (message.body().getString("error") != null) ? 400 : ebOption.statusCodeInSuccess();
			response.setStatusCode(code);
			response.setStatusMessage(ServerUtils.statusMessageByCode(code));
			response.headers().set(ebOption.headers(message.body()));
			response.end(message.body().encode());
		}
	};
}
