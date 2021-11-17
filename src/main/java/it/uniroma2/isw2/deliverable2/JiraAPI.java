package it.uniroma2.isw2.deliverable2;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import it.uniroma2.isw2.deliverable2.entities.Version;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JiraAPI {
		
	private String projectName;
	
	public JiraAPI(String projectName) {
		this.projectName = projectName;
	}
	
	public String getJSONResult() throws IOException {
		
		final String URL = "https://issues.apache.org/jira/rest/api/2/project/" + this.projectName;
		
		OkHttpClient client = new OkHttpClient();
		Request req = new Request.Builder()
				.url(URL)
				.build();
		
		Response res = client.newCall(req).execute();
		SimpleLogger.logInfo("Retrieved results from JIRA (URL: {0})",  URL);

		return res.body().string();
	}
	
	public List<Version> getVersions() throws IOException {
		List<Version> versions = new ArrayList<>();

		JsonElement body = JsonParser.parseString(this.getJSONResult());
		JsonArray jsonVersions = body.getAsJsonObject().get("versions").getAsJsonArray();
		
		jsonVersions.forEach(element -> {
			JsonObject jsonVersion = element.getAsJsonObject();
			LocalDateTime date = LocalDate.parse(jsonVersion.get("releaseDate").getAsString()).atStartOfDay();
			String id = jsonVersion.get("id").getAsString();
			String name = jsonVersion.get("name").getAsString();
			
			SimpleLogger.logInfo("New version inserted [ID: {0}, Name: {1}, Date: {2}]",
					new Object[] {id, name, date});
			
			Version version = new Version(id, name, date);
			versions.add(version);
		});
		
		versions.sort((v1, v2) -> v1.getDate().compareTo(v2.getDate()));
		SimpleLogger.logInfo("Founded {0} versions", versions.size());
		
		versions.subList((int)Math.ceil(versions.size()/2.0), versions.size()).clear();
		SimpleLogger.logInfo("Considered versions: {0}", versions.size());
		
		return versions;
	}
}
