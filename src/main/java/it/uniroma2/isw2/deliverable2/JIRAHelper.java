package it.uniroma2.isw2.deliverable2;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import it.uniroma2.isw2.deliverable2.entities.Bug;
import it.uniroma2.isw2.deliverable2.entities.Version;

public class JIRAHelper {
		
	private String projectName;
	
	public JIRAHelper(String projectName) {
		this.projectName = projectName;
	}
	
	public List<Version> getVersions() throws JsonSyntaxException, IOException {
		List<Version> versions = new ArrayList<>();
		
		final String URL = "https://issues.apache.org/jira/rest/api/2/project/" + this.projectName;
		JsonArray jsonVersions = RestHelper.getJSONObject(URL).get("versions").getAsJsonArray();
		for (JsonElement element : jsonVersions) {
			JsonObject jsonVersion = element.getAsJsonObject(); 
			if (jsonVersion.get("releaseDate") == null)
				continue;
			
			LocalDateTime date = LocalDate.parse(jsonVersion.get("releaseDate").getAsString()).atStartOfDay();
			String name = jsonVersion.get("name").getAsString();
			
			Version version = new Version(name, date);
			versions.add(version);
		}
		
		versions.sort((v1, v2) -> v1.getReleaseDate().compareTo(v2.getReleaseDate()));
		
		return versions;
	}
	
	private String getBugsURL(int startIndex, int maxResults) {
		return new StringBuilder("https://issues.apache.org/jira/rest/api/2/search?jql=")
				.append("project=").append(this.projectName)
				.append("%20AND%20issueType=Bug%20AND%20resolution=Fixed%20AND%20status%20in%20(Resolved,Closed)&fields=fixVersions,versions,created")
				.append("&startAt=").append(startIndex)
				.append("&maxResults=").append(maxResults)
				.toString();
	}
	
	public List<Bug> getBugs(List<Version> versions) throws IOException {
		
		final int MAX_RESULTS = 1000;
		
		int visited = 0;
		int total = 0;
		List<Bug> bugs = new ArrayList<>();	
		
		do {
			JsonObject response = RestHelper.getJSONObject(this.getBugsURL(visited, MAX_RESULTS));
			JsonArray jsonIssues = response.get("issues").getAsJsonArray(); 
			total = response.get("total").getAsInt();
			
			for (JsonElement element : jsonIssues) {
				JsonObject jsonIssue = element.getAsJsonObject();
				Bug bug = Bug.fromJsonObject(jsonIssue, versions);
				if (bug != null)
					bugs.add(bug);
			}
	
			visited += jsonIssues.size();
		} while(visited<total);

		return bugs;
	}
}
