package main.java.me.ssky.file;

import java.io.File;

import main.java.me.ssky.server.ServerMain;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

public class RetrievingFileHandler implements Handler<HttpServerRequest> {
	@Override
	public void handle(final HttpServerRequest request) {
		String dirName = request.path().split("/")[3];
		final String fileName = request.path().split("/")[4];

		String path = "./../server/files/" + dirName + "/" + fileName;
		File file = new File(path);
		if (file.exists()) {
			request.response().sendFile(path);
		} else {
			JsonObject option = new JsonObject();
			option.putString("action", "findone");
			option.putString("collection", "_Files");
			option.putObject("matcher", new JsonObject().putString("fileName", fileName));
			ServerMain._vertx.eventBus().send(ServerUtils.MONGO_PERSISTOR_ADDRESS, option, new Handler<Message<JsonObject>>() {
				@Override
				public void handle(Message<JsonObject> result) {
					final int size = result.body().getObject("result").getNumber("size").intValue();
					final JsonObject option = new JsonObject();
					option.putString("action", "getChunk");
					option.putString("files_id", fileName);
					option.putBoolean("reply", true);
					option.putNumber("n", 0);
					final Buffer buffer = new Buffer(0);
					request.response().setChunked(true);
					ServerMain._vertx.eventBus().send(ServerUtils.MONGO_GRIDFS_ADDRESS, option, new Handler<Message<byte[]>>() {
						private Handler<Message<byte[]>> replyHandler = new Handler<Message<byte[]>>() {
							@Override
							public void handle(Message<byte[]> result) {
								if (result.body().length == 0 || buffer.length() >= size) {
									request.response().end();
								} else {
									request.response().write(new Buffer(result.body()));
								}
								result.reply(replyHandler);
							}
						};

						@Override
						public void handle(Message<byte[]> result) {
							request.response().write(new Buffer(result.body()));
							result.reply(replyHandler);
						}
					});
				}
			});
		}
	}

}
