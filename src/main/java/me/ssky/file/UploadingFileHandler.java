package main.java.me.ssky.file;

import java.io.File;
import java.util.UUID;

import main.java.me.ssky.server.ServerMain;
import main.java.me.ssky.util.ServerUtils;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Base64;

public class UploadingFileHandler implements Handler<HttpServerRequest> {

	@Override
	public void handle(final HttpServerRequest request) {
		final String fileId = UUID.randomUUID().toString();
		final String token = UUID.randomUUID().toString();
		File dir = new File(token);
		if (!dir.exists()) {
			dir.mkdir();
		}

		final String fileName = fileId + "-" + request.path().split("/")[3];
		final UploadingFileHelper helper = new UploadingFileHelper();

		final String path = token + "/" + fileName;
		final String url = "http://localhost/1/files/" + path;

		if (request.headers().get("origin") != null) {
			request.bodyHandler(new Handler<Buffer>() {
				@Override
				public void handle(Buffer buffer) {
					JsonObject data = new JsonObject(buffer.toString());
					if (Base64.decodeToFile(data.getString("base64"), path)) {
						JsonObject reply = new JsonObject();
						reply.putString("url", url);
						reply.putString("name", fileName);
						ServerUtils.responseHeaders(201);

						HttpServerResponse response = request.response();
						response.setStatusCode(201);
						response.setStatusMessage(ServerUtils.statusMessageByCode(201));
						response.headers().set(ServerUtils.responseHeaders(201));
						response.end(reply.encode());
					}
				}
			});
			return;
		}

		request.dataHandler(new Handler<Buffer>() {
			@Override
			public void handle(Buffer buffer) {
				ServerMain._vertx.eventBus().send(ServerUtils.MONGO_GRIDFS_ADDRESS + "/saveChunk", helper.getMessage(fileName, helper.getN(), buffer.getBytes()));
				helper.addOne();
				helper.addSize(buffer.length());
			}
		});

		request.endHandler(new Handler<Void>() {
			@Override
			public void handle(Void _void) {
				JsonObject document = new JsonObject();

				document.putString("dirName", token);
				document.putString("fileName", fileName);
				document.putNumber("size", helper.getSize());
				document.putNumber("n", helper.getN());

				JsonObject option = new JsonObject();
				option.putString("action", "save");
				option.putString("collection", "_Files");
				option.putObject("document", document);
				ServerMain._vertx.eventBus().send(ServerUtils.MONGO_PERSISTOR_ADDRESS, option, new Handler<Message<JsonObject>>() {
					@Override
					public void handle(Message<JsonObject> result) {
						JsonObject reply = new JsonObject();
						reply.putString("url", url);
						reply.putString("name", fileName);
						ServerUtils.responseHeaders(201);

						HttpServerResponse response = request.response();
						response.setStatusCode(201);
						response.setStatusMessage(ServerUtils.statusMessageByCode(201));
						response.end(reply.encode());
					}
				});
			}
		});
	}
}

/*
 * curl -X POST \ -H "X-Parse-Application-Id: YUH91TBS1TBOKVrgDRRxvmUi95luezVVQqNW5j0x" \ -H "X-Parse-REST-API-Key: J7eLuRAewq5Dhaw99NNHYO9ejgsuSfmUSCpwqfF9" \ -H "Content-Type: image/jpeg" \
 * --data-binary '@myPicture.jpg' \ http://localhost/1/files/pic.jpg
 */
