package it.uniroma2.isw2.deliverable2;

import java.io.IOException;

import com.google.gson.JsonSyntaxException;

public class Analyser {
	
	private JiraAPI jiraAPI;
	
	public Analyser(String projectName) {
		this.jiraAPI = new JiraAPI(projectName);
	}
	
	public void run() throws JsonSyntaxException, IOException {
		this.jiraAPI.getVersions();
	}
	
	public static void main(String[] args) throws JsonSyntaxException, IOException {
		Analyser a = new Analyser(Parameters.PROJECT_NAME);
		a.run();
	}
	
}