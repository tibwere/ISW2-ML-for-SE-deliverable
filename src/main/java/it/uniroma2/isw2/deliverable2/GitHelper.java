package it.uniroma2.isw2.deliverable2;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.uniroma2.isw2.deliverable2.entities.Commit;
import it.uniroma2.isw2.deliverable2.entities.Diff;
import it.uniroma2.isw2.deliverable2.exceptions.MaximumRequestToGithubAPIException;
import it.uniroma2.isw2.deliverable2.exceptions.MissingGithubTokenException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GitHelper {
	private static final String TOKEN_PATH = "token.key";
	private static final String CACHE_COMMIT_LIST = ".cache/commit-list/%s/%s.json";
	private static final String CACHE_COMMIT_INFO = ".cache/commit-info/%s/%s.json";
	private static final String REMOTE_COMMIT_LIST = "https://api.github.com/repos/apache/%s/commits?per_page=100&page=%d";
	private static final String REMOTE_COMMIT_INFO = "https://api.github.com/repos/apache/%s/commits/%s";

	/* To avoid Sonar Smells */
	private static final String MESSAGE_STR = "message";
	private static final String SHA_STR = "sha";
	private static final String COMMIT_STR = "commit";
	private static final String AUTHOR_STR = "author";
	private static final String NAME_STR = "name";
	private static final String DATE_STR = "date";
	private static final String FILES_STR = "files";
	private static final String FILENAME_STR = "filename";
	private static final String JAVA_EXT_STR = ".java";
	private static final String ADDITIONS_STR = "additions";
	private static final String DELETIONS_STR = "deletions";

	private String token;
	private String projectName;
	
	public GitHelper(String projectName) throws MissingGithubTokenException {
		this.projectName = projectName.toLowerCase();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(TOKEN_PATH))) {
			this.token = reader.readLine();
		} catch (IOException e) {
			throw new MissingGithubTokenException();
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
				String sha = jsonCommit.get(SHA_STR).getAsString();
				commits.add(0, sha);
			});
			index++;
			
		}while(results>0);

		return commits;
	}
	
	public List<Commit> getCommits(LocalDateTime targetDate) throws IOException, MaximumRequestToGithubAPIException {
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
	
	private Commit getCommit(String sha) throws IOException, MaximumRequestToGithubAPIException {
		String cache = String.format(CACHE_COMMIT_INFO, this.projectName, sha);
		String remote = String.format(REMOTE_COMMIT_INFO, this.projectName, sha);
		
		Commit c = new Commit();
		JsonObject jsonResponse = RestHelper.getJSONObject(remote, this.token, cache);

		JsonObject jsonCommit = jsonResponse.get(COMMIT_STR).getAsJsonObject();
		JsonObject jsonAuthor = jsonCommit.get(AUTHOR_STR).getAsJsonObject();
		
		c.setSha(sha);
		c.setAuthor(jsonAuthor.getAsJsonObject().get(NAME_STR).getAsString());
		c.setDate(jsonAuthor.get(DATE_STR).getAsString());
		c.setMessage(jsonCommit.get(MESSAGE_STR).getAsString());
		
		JsonArray jsonDiffs = jsonResponse.get(FILES_STR).getAsJsonArray();
		jsonDiffs.forEach(element -> {
			JsonObject jsonDiff = element.getAsJsonObject();
			Diff d = new Diff();
			String filename = jsonDiff.get(FILENAME_STR).getAsString();
			
			if (filename.endsWith(JAVA_EXT_STR)) {
				d.setFilename(filename);
				d.setAdditions(jsonDiff.get(ADDITIONS_STR).getAsInt());
				d.setDeletions(jsonDiff.get(DELETIONS_STR).getAsInt());
				c.addDiff(d);	
			}
		});
		
		return c;
	}
}
