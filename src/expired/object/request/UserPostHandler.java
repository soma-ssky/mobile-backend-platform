package expired.object.request;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class UserPostHandler implements Handler<Buffer> {
	private EventBus eb;
	private HttpServerRequest request;

	public void handle(EventBus eb, HttpServerRequest request, Buffer buffer) {
		this.eb = eb;
		this.request = request;
		handle(buffer);
	}

	public void handle(Buffer buffer) {
		JsonObject jsonData = new JsonObject(buffer.toString());
		switch (request.path()) {
		case "/join":
			join(request, jsonData);
			break;
		case "/drop":
			drop(request, jsonData);
			break;
		case "/getuser":
			getUser(request, jsonData);
			break;
		}
	}

	private void join(final HttpServerRequest request, JsonObject jsonData) {
		JsonObject joinData = new JsonObject();
		joinData.putString("action", "join").putObject("userdata", jsonData);
		eb.send("vertx.user", joinData, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				replyJson(request, result.body());
			}
		});
	}

	private void drop(final HttpServerRequest request, JsonObject jsonData) {
		eb.send("vertx.user", jsonData.putString("action", "drop"), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				replyJson(request, result.body());
			}
		});
	}

	private void getUser(final HttpServerRequest request, JsonObject jsonData) {
		eb.send("vertx.user", jsonData.putString("action", "getuser"), new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				replyJson(request, result.body());
			}
		});
	}

	private void replyJson(HttpServerRequest request, JsonObject jsonMsg) {
		request.response().putHeader("Content-Type", "application/json");
		request.response().putHeader("Content-length", jsonMsg.toString().length() + "");
		request.response().end(jsonMsg.toString());
	}
}
