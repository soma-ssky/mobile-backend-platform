package main.java.handler;

import java.util.UUID;

import main.java.role.FileUploadHelper;
import main.java.server.ServerMain;
import main.java.util.Util;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Base64;
import org.vertx.java.platform.Verticle;

public class FileUploadHandler extends Verticle implements Handler<HttpServerRequest> {

	@Override
	public void handle(final HttpServerRequest request) {
		final String fileId = UUID.randomUUID().toString();
		final String fileName = fileId + "_" + request.path().split("/")[3];
		final FileUploadHelper helper = new FileUploadHelper();
		if (request.headers().get("origin") != null) {
			request.bodyHandler(new Handler<Buffer>() {
				@Override
				public void handle(Buffer buffer) {
					JsonObject data = new JsonObject(buffer.toString());
					if (Base64.decodeToFile(data.getString("base64"), fileName)) {
						request.response().setStatusCode(201).end();
					}
				}
			});
			return;
		}

		request.dataHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				ServerMain.eb.send(Util.MONGO_GRIDFS_ADDRESS + "/saveChunk", helper.getMessage(fileId, helper.getN(), buffer.getBytes()));
				helper.addOne();
				helper.addSize(buffer.length());
			}
		});

		request.endHandler(new Handler<Void>() {
			@Override
			public void handle(Void _void) {
				JsonObject result = new JsonObject();
				double size = helper.getSize();
				result.putNumber("received_size", size);
				result.putString("file_id", fileId);
				result.putString("new_name", fileName);
				result.putNumber("chunk_num", helper.getN());
				request.response().setStatusCode(201).end(result.encode());
			}
		});

	}
}
