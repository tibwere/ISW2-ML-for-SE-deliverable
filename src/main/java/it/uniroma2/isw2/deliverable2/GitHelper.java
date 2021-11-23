package it.uniroma2.isw2.deliverable2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import it.uniroma2.isw2.deliverable2.entities.Commit;
import it.uniroma2.isw2.deliverable2.entities.Diff;

public class GitHelper {
	private static final String TOKEN_PATH = "token.key";
	private static final String CACHE_COMMIT_LIST = ".cache/commit-list/%s/%s.json";
	private static final String CACHE_COMMIT_INFO = ".cache/commit-info/%s/%s.json";
	private static final String REMOTE_COMMIT_LIST = "https://api.github.com/repos/apache/%s/commits?per_page=100&page=%d";
	private static final String REMOTE_COMMIT_INFO = "https://api.github.com/repos/apache/%s/commits/%s";
	
	private String token;
	private String projectName;
	
	public GitHelper(String projectname) throws IOException {
		this.projectName = projectname.toLowerCase();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(TOKEN_PATH))) {
			this.token = reader.readLine();
		}
	}
	
	private List<String> getCommitsSHA() throws IOException {
		List<String> commits = new ArrayList<>();
		
		int index = 1;
		int results = 0;
		do {
			String cache = String.format(CACHE_COMMIT_LIST, this.projectName, index);
			String remote = String.format(REMOTE_COMMIT_LIST, this.projectName, index);
			
			JsonArray jsonCommits = RestHelper.getJSONArray(remote, this.token, cache);
			results = jsonCommits.size();
			
			jsonCommits.forEach(element -> {
				JsonObject jsonCommit = element.getAsJsonObject();
				String sha = jsonCommit.get("sha").getAsString();			
				commits.add(0, sha);
			});
			index++;
			
		}while(results>0);

		return commits;
	}
	
	public List<Commit> getCommits(LocalDateTime targetDate) throws IOException {
		List<Commit> commits = new ArrayList<>();
		Iterator<String> shas = getCommitsSHA().iterator();
		boolean targetDateReached = false;
		
		while(shas.hasNext() && !targetDateReached) {
			Commit c = getCommit(shas.next());
			if (c.getDate().isAfter(targetDate))
				targetDateReached = true;
			else
				commits.add(c);
		}
				
		return commits;
	}
	
	private Commit getCommit(String sha) throws IOException {
		String cache = String.format(CACHE_COMMIT_INFO, this.projectName, sha);
		String remote = String.format(REMOTE_COMMIT_INFO, this.projectName, sha);
		
		Commit c = new Commit();		
		JsonObject jsonCommit = RestHelper.getJSONObject(remote, this.token, cache);
		JsonObject jsonAuthorObj = jsonCommit.get("commit").getAsJsonObject().get("author").getAsJsonObject();
		
		c.setSha(sha);
		c.setAuthor(jsonAuthorObj.get("name").getAsString());
		c.setDate(jsonAuthorObj.get("date").getAsString());
		
		JsonArray jsonDiffs = jsonCommit.get("files").getAsJsonArray();
		jsonDiffs.forEach(element -> {
			JsonObject jsonDiff = element.getAsJsonObject();
			Diff d = new Diff();
			String filename = jsonDiff.get("filename").getAsString();
			
			if (filename.endsWith(".java")) {
				d.setFilename(filename);
				d.setAdditions(jsonDiff.get("additions").getAsInt());
				d.setDeletions(jsonDiff.get("deletions").getAsInt());
				c.addDiff(d);	
			}
		});
		
		return c;
	}
}
