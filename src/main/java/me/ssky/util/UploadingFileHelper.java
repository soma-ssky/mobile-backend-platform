package main.java.me.ssky.util;

import java.io.UnsupportedEncodingException;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;

public class UploadingFileHelper {
	int n = 0;
	long size = 0;

	public void addOne() {
		n++;
	}

	public int getN() {
		return n;
	}

	public void addSize(int size) {
		this.size += (long) size;
	}

	public long getSize() {
		return size;
	}

	public Buffer getMessage(String files_id, int chunkNumber, byte[] data) {
		JsonObject jsonObject = new JsonObject().putString("files_id", files_id).putNumber("n", chunkNumber);
		byte[] json;
		try {
			json = jsonObject.encode().getBytes("UTF-8");
			Buffer buffer = new Buffer();
			buffer.appendInt(json.length);
			buffer.appendBytes(json);
			buffer.appendBytes(data);

			return buffer;

		} catch (UnsupportedEncodingException e) {
			return null;
		}

	}

}
