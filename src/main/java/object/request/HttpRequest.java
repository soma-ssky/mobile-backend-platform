package main.java.object.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import main.java.object.exception.SskyException;
import main.java.object.object.SskyObject;
import main.java.object.util.Util;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

public class HttpRequest {
	private static final String METHOD_POST = "POST";
	private static final String METHOD_DELETE = "DELETE";
	private static final String METHOD_PUT = "PUT";
	private static final String METHOD_GET = "GET";

	private static final String CREATE_AT = "createAt";
	private static final String LAST_UPDATE_AT = "lastUpdateAt";

	private static final int VERSION = 1;
	private static final int PORT = 8080;
	private static final String HOST = "http://localhost";
	private static final String PREFIX = "classes";

	public void create(SskyObject obj) throws SskyException {
		String data = new JsonObject(obj.getData()).toString();
		String reply = null;
		try {
			HttpURLConnection connection = getConnection(obj.getClassName(), METHOD_POST, true, true, data.length());
			write(connection, data);
			reply = read(connection);
		} catch (Exception e) {
			throw new SskyException(0);
		}

		JsonObject replyJson = new JsonObject(reply);
		if (replyJson.getString("status").equals("error")) throw new SskyException(0);
		obj.setObjectId(replyJson.getString("_id"));
		obj.setCreateAt(Util.convertDateToString(replyJson.getString(CREATE_AT)));
		obj.setLastUpdateAt(Util.convertDateToString(replyJson.getString(LAST_UPDATE_AT)));
	}

	public void delete(String className, String objectId) throws SskyException {
		String reply = null;
		try {
			HttpURLConnection connection = getConnection(className + "/" + objectId, METHOD_DELETE, true, false, 0);
			reply = read(connection);
		} catch (Exception e) {
			throw new SskyException(0);
		}
		JsonObject replyJson = new JsonObject(reply);
		if (replyJson.getString("status").equals("error")) throw new SskyException(0);
	}

	public void update(SskyObject obj) throws SskyException {
		String data = new JsonObject(obj.getData()).putString("createAt", obj.getCreateAt().toString()).toString();
		String reply = null;
		try {
			HttpURLConnection connection = getConnection(obj.getClassName() + "/" + obj.getObjectId(), METHOD_PUT, true, true, data.length());
			write(connection, data);
			reply = read(connection);
		} catch (Exception e) {
			throw new SskyException(0);
		}

		JsonObject replyJson = new JsonObject(reply);
		if (replyJson.getString("status").equals("error")) throw new SskyException(0);
		obj.setLastUpdateAt(Util.convertDateToString(replyJson.getString(LAST_UPDATE_AT)));
	}

	@SuppressWarnings("unchecked")
	public <T extends SskyObject> T retrieve(String className, String objectId) throws SskyException {
		String reply = null;
		try {
			HttpURLConnection connection = getConnection(className + "/" + objectId, METHOD_GET, true, true, 0);
			reply = read(connection);
		} catch (Exception e) {
			throw new SskyException(0);
		}

		JsonObject replyJson = new JsonObject(reply);
		if (replyJson.getString("status").equals("error")) throw new SskyException(0);
		SskyObject obj = new SskyObject(className);
		if (replyJson.getObject("result") == null) return null;
		obj.setObjectId(replyJson.getObject("result").getString("_id"));
		obj.setCreateAt(Util.convertDateToString(replyJson.getObject("result").getString(CREATE_AT)));
		obj.setLastUpdateAt(Util.convertDateToString(replyJson.getObject("result").getString(LAST_UPDATE_AT)));
		obj.putAll(replyJson.getObject("result").toMap());

		return (T) obj;
	}

	@SuppressWarnings("unchecked")
	public <T extends SskyObject> List<T> query(String className, JsonObject matcher, JsonObject option) throws SskyException {
		String reply = null;
		try {
			HttpURLConnection connection = getConnection(className + "?where=" + matcher.toString() + "&option=" + option.toString(), METHOD_GET, true, false, 0);
			reply = read(connection);
		} catch (Exception e) {
			throw new SskyException(0);
		}
		JsonArray replysJson = new JsonObject(reply).getArray("results");
		if (new JsonObject(reply).getString("status").equals("error")) throw new SskyException(0);
		ArrayList<T> list = new ArrayList<T>();
		for (int i = 0; i < replysJson.size(); i++) {
			SskyObject obj = new SskyObject(className);
			JsonObject replyJson = replysJson.get(i);
			obj.setObjectId(replyJson.getString("_id"));
			obj.setCreateAt(Util.convertDateToString(replyJson.getString(CREATE_AT)));
			obj.setLastUpdateAt(Util.convertDateToString(replyJson.getString(LAST_UPDATE_AT)));
			obj.putAll(replyJson.toMap());
			list.add((T) obj);
		}
		return list;

	}

	private HttpURLConnection getConnection(String pUrl, String method, boolean doInput, boolean doOutput, int length) throws Exception {
		HttpURLConnection connection = getConnection(pUrl, method, doInput, doOutput);
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("Content-Length", "" + length);
		return connection;
	}

	private HttpURLConnection getConnection(String pUrl, String method, boolean doInput, boolean doOutput) throws Exception {
		URL url = new URL(HOST + ":" + PORT + "/" + VERSION + "/" + PREFIX + "/" + pUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		connection.setDoOutput(doOutput);
		connection.setDoInput(doInput);

		return connection;
	}

	private void write(HttpURLConnection connection, String str) throws IOException {
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(str);
			writer.flush();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private String read(HttpURLConnection connection) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String reply = "";
			String line;
			while ((line = reader.readLine()) != null) {
				reply += line;
			}
			return reply;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
}