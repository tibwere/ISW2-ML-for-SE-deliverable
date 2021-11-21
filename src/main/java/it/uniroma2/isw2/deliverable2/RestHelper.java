package it.uniroma2.isw2.deliverable2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RestHelper {
	
	private RestHelper() {
		throw new IllegalStateException("This class should not be instantiated");
	}

	public static JsonObject getJSONObject(String url) throws IOException {
		OkHttpClient client = new OkHttpClient();
		Request req = new Request.Builder().url(url).build();

		Response res = client.newCall(req).execute();
		SimpleLogger.logInfo("Retrieved results from {0}", url);

		return JsonParser.parseString(res.body().string()).getAsJsonObject();
	}

	public static JsonObject getJSONObject(String url, String token, String cache) throws IOException {
		String body = getJSON(url, token, cache);
		JsonObject obj = JsonParser.parseString(body).getAsJsonObject();

		if (!Files.exists(Paths.get(cache)) && obj.size() > 0)
			cacheResponse(cache, body);

		return obj;

	}

	public static JsonArray getJSONArray(String url, String token, String cache) throws IOException {
		String body = getJSON(url, token, cache);
		JsonArray arr = JsonParser.parseString(body).getAsJsonArray();

		if (!Files.exists(Paths.get(cache)) && arr.size() > 0)
			cacheResponse(cache, body);

		return arr;
	}
	
	private static String getJSON(String url, String token, String cache) throws IOException {
		Path cachePath = Paths.get(cache);

		if (Files.exists(cachePath)) {
			SimpleLogger.logInfo("Retrieved results from cache ({0})", cache);
			return Files.readString(cachePath);
		} else {
			OkHttpClient client = new OkHttpClient();
			Request req = new Request.Builder().url(url).header("Authorization", "token " + token).build();

			Response res = client.newCall(req).execute();
			SimpleLogger.logInfo("Retrieved results from: {0}", url);
			return res.body().string();
		}
	}
	
	private static void cacheResponse(String cacheFile, String body) throws IOException {
		Path cachePath = Paths.get(cacheFile);
		Files.createDirectories(cachePath.getParent());
		Files.createFile(cachePath);
		Files.writeString(cachePath, body, StandardCharsets.UTF_8);
		SimpleLogger.logInfo("Cached results in {0}", cacheFile);
	}
}
