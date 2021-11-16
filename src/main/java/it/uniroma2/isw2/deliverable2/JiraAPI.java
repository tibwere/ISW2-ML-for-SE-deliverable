package it.uniroma2.isw2.deliverable2;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import it.uniroma2.isw2.deliverable2.entities.Version;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JiraAPI {
	
	private static final Logger LOGGER = Logger.getLogger("ISW2-deliverable-2");
	private String projectName;
	
	public JiraAPI(String projectName) {
		this.projectName = projectName;
	}
	
	private String getJSONResult() throws IOException {
		final String URL = "https://issues.apache.org/jira/rest/api/2/project/" + this.projectName;
		
		OkHttpClient client = new OkHttpClient();
		Request req = new Request.Builder()
				.url(URL)
				.build();
		
		Response res = client.newCall(req).execute();
		LOGGER.log(Level.INFO, "Retrieved results from JIRA");
		return res.body().string();
	}
	
	public List<Version> getVersions() throws JsonSyntaxException, IOException {
		List<Version> versions = new ArrayList<>();
		
		JsonElement body = JsonParser.parseString(this.getJSONResult());
		JsonArray jsonVersions = body.getAsJsonObject().get("versions").getAsJsonArray();
		
		jsonVersions.forEach(element -> {
			JsonObject jsonVersion = element.getAsJsonObject();
			LocalDateTime date = LocalDate.parse(jsonVersion.get("releaseDate").getAsString()).atStartOfDay();
			String id = jsonVersion.get("id").getAsString();
			String name = jsonVersion.get("name").getAsString();
			
			LOGGER.log(Level.INFO, "New version inserted [ID: {0}, Name: {1}, Date: {2}]",
					new Object[] {id, name, date});
			
			Version version = new Version(id, name, date);
			versions.add(version);
		});
		
		versions.sort((v1, v2) -> v1.getDate().compareTo(v2.getDate()));
		
		LOGGER.log(Level.INFO, "Founded {0} versions", versions.size());
		
		return versions;
	}
}
