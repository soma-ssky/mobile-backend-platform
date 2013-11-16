package main.java.me.ssky.server;

import java.util.Iterator;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class StoryVerticle extends Verticle implements Handler<Message<JsonObject>> {
	private EventBus eb;
	private String objAddress;
	private String relationCollection;
	private String postCollection;

	@Override
	public void start() {
		eb = vertx.eventBus();

		JsonObject config = container.config();
		String thisAddress = config.getString("thisAddress");
		objAddress = config.getString("objAddress");
		relationCollection = config.getString("relationCollection");
		postCollection = config.getString("postCollection");

		vertx.eventBus().registerHandler(thisAddress, this);
	}

	@Override
	public void handle(Message<JsonObject> message) {
		String action = message.body().getString("action");
		if (action == null) {
			message.reply("action must be specified");
			return;
		}
		switch (action) {
		case "follow":
			follow(message);
			break;
		case "unfollow":
			unfollow(message);
			break;
		case "getFollowerList":
			getMyFollowingList(message);
			break;
		case "getPostList":
			getFollowerPostList(message);
			break;
		default:
			message.reply("Invalid action: " + action);
		}
	}

	private void follow(final Message<JsonObject> message) {
		final String myId = message.body().getString("myId");
		final String friendId = message.body().getString("friendId");

		JsonObject documents = new JsonObject();
		documents.putString("from", myId);
		documents.putString("to", friendId);

		JsonObject option = new JsonObject();
		option.putString("action", "save");
		option.putString("collection", relationCollection);
		option.putObject("documents", documents);
		eb.send(objAddress, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				message.reply(result.body());
			}
		});
	}

	private void unfollow(final Message<JsonObject> message) {
		final String myId = message.body().getString("myId");
		final String friendId = message.body().getString("friendId");

		JsonObject matcher = new JsonObject();
		matcher.putString("from", myId);
		matcher.putString("to", friendId);

		JsonObject option = new JsonObject();
		option.putString("action", "delete");
		option.putString("collection", relationCollection);
		option.putObject("matcher", matcher);

		eb.send(objAddress, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				message.reply(result.body());
			}
		});
	}

	private void getMyFollowingList(final Message<JsonObject> message) {
		String myId = message.body().getString("myId");

		JsonObject option = new JsonObject();
		option.putString("action", "retrieve");
		option.putString("collection", relationCollection);
		option.putObject("matcher", new JsonObject().putString("from", myId));

		eb.send(objAddress, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				JsonArray replyArray = new JsonArray();
				Iterator<Object> itr = result.body().getArray("results").iterator();
				while (itr.hasNext()) {
					JsonObject obj = (JsonObject) itr.next();
					replyArray.add(obj.getString("to"));
				}
				message.reply(new JsonObject().putArray("results", replyArray));
			}
		});
	}

	private void getFollowerPostList(final Message<JsonObject> message) {
		String myId = message.body().getString("myId");

		JsonObject option = new JsonObject();
		option.putString("action", "retrieve");
		option.putString("collection", relationCollection);
		option.putObject("matcher", new JsonObject().putString("from", myId));

		eb.send(objAddress, option, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> result) {
				JsonArray orArray = new JsonArray();
				Iterator<Object> itr = result.body().getArray("results").iterator();
				while (itr.hasNext()) {
					JsonObject obj = (JsonObject) itr.next();
					orArray.add(new JsonObject().putString("writer", obj.getString("to")));
				}

				JsonObject option = new JsonObject();
				option.putString("action", "retrieve");
				option.putString("collection", postCollection);
				option.putObject("matcher", new JsonObject().putArray("$or", orArray));
				option.putString("order", "-createdAt");
				eb.send(objAddress, option, new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> result) {
						message.reply(result.body());
					}
				});
			}
		});
	}
}
