package it.uniroma2.isw2.deliverable2;

import java.io.IOException;
import java.util.List;

import it.uniroma2.isw2.deliverable2.entities.Version;

public class Analyser {
	
	private static final String PROJECT_NAME = "BOOKKEEPER";

	private JiraAPI jiraAPI;
	private GitAPI gitAPI;
	
	public Analyser(String projectName) {
		this.jiraAPI = new JiraAPI(projectName);
		this.gitAPI = new GitAPI(projectName);
	}
	
	public void run() throws IOException, InterruptedException {
		List<Version> versions = this.jiraAPI.getVersions();
		this.gitAPI.cloneRepository();
		this.gitAPI.setupTargetRevision(versions.get(versions.size()-1).getDate());
		this.gitAPI.recuresivelyGetFileNames();
		cleanup();
	}
	
	private void cleanup() throws IOException {
		this.gitAPI.removeLocalRepository();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Analyser a = new Analyser(PROJECT_NAME);
		a.run();
	}
	
}