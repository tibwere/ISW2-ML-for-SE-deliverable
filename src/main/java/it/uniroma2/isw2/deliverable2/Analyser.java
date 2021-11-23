package it.uniroma2.isw2.deliverable2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonSyntaxException;

import it.uniroma2.isw2.deliverable2.entities.Bug;
import it.uniroma2.isw2.deliverable2.entities.Commit;
import it.uniroma2.isw2.deliverable2.entities.DatasetEntry;
import it.uniroma2.isw2.deliverable2.entities.Diff;
import it.uniroma2.isw2.deliverable2.entities.Version;

public class Analyser {
	
	private String project;
	
	private GitHelper gitHelper;
	private JIRAHelper jiraHelper;
	
	private List<Version> versions;
	private List<Commit> commits;
	private List<Bug> bugs;
	private Map<String, DatasetEntry> files;
	
	private static final String RESULTS_FOLDER = "results/";
	
	public Analyser(String project) throws IOException {
		this.project = project;
		this.gitHelper = new GitHelper(this.project);
		this.jiraHelper = new JIRAHelper(this.project);
		
		this.setupResultsFolder();
	}
	
	public void run() throws IOException {
		/* Init attributes */
		int targetVersionIdx = this.initAnalysis();
		
		/* Create dataset */
		this.createDataset(targetVersionIdx);		
	}
	
	private int initAnalysis() throws JsonSyntaxException, IOException {
		this.versions = jiraHelper.getVersions();
		this.bugs = jiraHelper.getBugs(this.versions);
		int mid = (int)Math.ceil(this.versions.size()/2.0);
		this.commits = gitHelper.getCommits(this.versions.get(mid).getReleaseDate());
		this.files = new HashMap<>();
		
		return mid;
	}
	
	private void createDataset(int targetVersionIndex) throws IOException {
		int versionsIdx = 0;
		int commitsIdx = 0;
				
		while (versionsIdx < targetVersionIndex && commitsIdx < this.commits.size()) {			
			if (this.needSwitchVersion(versionsIdx, commitsIdx)) {
				this.evalStatistics(commitsIdx);
				this.resetFilesForNewVersion(++versionsIdx);
			}
			
			this.applyDiff(versionsIdx, commitsIdx++);
		}
		
		// Dump pending commits
		this.evalStatistics(commitsIdx);
	}
	
	private void applyDiff(int versionsIdx, int commitsIdx) {
		for (Diff d : this.commits.get(commitsIdx).getDiffs()) {
			String key = d.getFilename();
			
			DatasetEntry entry = null;
			if (this.files.containsKey(key))
				entry = this.files.get(key);
			else
				entry = new DatasetEntry(versions.get(versionsIdx).getName(), d.getFilename(), commits.get(commitsIdx).getDate());
			
			entry.updateChurn(d.getAdditions(), d.getDeletions());
			entry.insertCommit(this.commits.get(commitsIdx));

			this.files.put(key, entry);
		}
	}
	
	private void resetFilesForNewVersion(int versionsIdx) {
		Map<String, DatasetEntry> newEntries = new HashMap<>();
		
		for (DatasetEntry entry : this.files.values()) {
			DatasetEntry newEntry = new DatasetEntry(versions.get(versionsIdx).getName(), entry.getName(), entry.getBirth(), entry.getSize());
			newEntries.put(entry.getName(), newEntry);
		}
		
		this.files = newEntries;
	}
	
	private boolean needSwitchVersion(int versionsIdx, int commitsIdx) {
		return this.commits.get(commitsIdx).getDate().isAfter(this.versions.get(versionsIdx+1).getReleaseDate());
	}
	
	private void evalStatistics(int commitsIdx) throws IOException {
		LocalDateTime date = (commitsIdx > 0) ? commits.get(commitsIdx-1).getDate() : null;

		Comparator<DatasetEntry> comparator = Comparator
				.comparing(DatasetEntry::getVersion)
				.thenComparing(DatasetEntry::getName);
		
		List<DatasetEntry> entries = new ArrayList<>(this.files.values());
		entries.sort(comparator);
		File dataset = new File(String.format("%s%s.csv", RESULTS_FOLDER, this.project));
		try (FileWriter writer = new FileWriter(dataset, true)) {
			for (DatasetEntry e : entries) {
				if (e.getSize() > 0)
					writer.append(String.format("%s%n", e.toCSV(date)));
			}
		}
	}
	
	private void setupResultsFolder() throws IOException {
		Path resultFolder = Paths.get(RESULTS_FOLDER);
		if (!Files.exists(resultFolder))
			Files.createDirectory(resultFolder);
		
		Path dataset = Paths.get(String.format("%s%s.csv", RESULTS_FOLDER, this.project));
		if (Files.exists(dataset))
			Files.delete(dataset);
		
		Files.createFile(dataset);
		Files.writeString(dataset, DatasetEntry.CSV_HEADER);
	}
	
}
