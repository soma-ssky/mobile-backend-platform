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

	public EventBusHandler(EventBusOption ebOption) {
		this.ebOption = ebOption;
	}

	@Override
	public void handle(final HttpServerRequest request) {
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				JsonObject data = new JsonObject(buffer.toString());
				ServerMain._vertx.eventBus().send(ebOption.address(), ebOption.option(request, data), new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> message) {
						HttpServerResponse response = request.response();
						int code = (message.body().getString("error") != null) ? 400 : ebOption.statusCodeInSuccess();
						response.setStatusCode(code);
						response.setStatusMessage(ServerUtils.statusMessageByCode(code));
						response.headers().set(ebOption.headers(message.body()));
						response.end(message.body().encode());
					}
				});
			}
		});
	}
}
