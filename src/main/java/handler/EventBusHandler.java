package main.java.handler;

import main.java.server.ServerMain;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class EventBusHandler implements Handler<HttpServerRequest> {
	private RequestDataOption dataProcessor;

	public EventBusHandler(RequestDataOption dataProcessor) {
		this.dataProcessor = dataProcessor;
	}

	@Override
	public void handle(final HttpServerRequest request) {
		request.bodyHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				String ebAddress = dataProcessor.getAddress();
				JsonObject option = dataProcessor.getOption(request, buffer);
				ServerMain.eb.send(ebAddress, option, new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> result) {
						request.response().end(result.body().encode());
					}
				});
			}
		});
	}
}
