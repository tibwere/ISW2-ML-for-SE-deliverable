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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonSyntaxException;

import it.uniroma2.isw2.deliverable2.entities.Bug;
import it.uniroma2.isw2.deliverable2.entities.Commit;
import it.uniroma2.isw2.deliverable2.entities.DatasetEntry;
import it.uniroma2.isw2.deliverable2.entities.Diff;
import it.uniroma2.isw2.deliverable2.entities.Version;

public class Analyser {
	
	private static final Logger LOGGER = Logger.getLogger("ISW2-DELIVERABLE-2");
	
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
		LOGGER.log(Level.INFO, "*** Retrieve tickets from JIRA ***");
		this.versions = jiraHelper.getVersions();
		LOGGER.log(Level.INFO, "Found {0} versions", this.versions.size());
		
		LOGGER.log(Level.INFO, "*** Retrieve bugs from JIRA ***");
		this.bugs = jiraHelper.getBugs(this.versions);
		LOGGER.log(Level.INFO, "*** Found {0} bugs", this.bugs.size());
		
		int targetIdx = (int)Math.ceil(this.versions.size()/2.0);
		LOGGER.log(Level.INFO, "Considering only first half ov versions ({0})", targetIdx);
		
		LOGGER.log(Level.INFO, "*** Retrieve commits for first {0} versions from Github ***", targetIdx);
		this.commits = gitHelper.getCommits(this.versions.get(targetIdx).getReleaseDate());
		LOGGER.log(Level.INFO, "*** Found {0} commits", this.commits.size());

		
		this.files = new HashMap<>();
		
		return targetIdx;
	}
	
	private void createDataset(int targetVersionIndex) throws IOException {
		int versionsIdx = 0;
		int commitsIdx = 0;
				
		while (versionsIdx < targetVersionIndex && commitsIdx < this.commits.size()) {			
			if (this.needSwitchVersion(versionsIdx, commitsIdx)) {
				LOGGER.log(Level.INFO, "After {0} commits switch from version {1} to {2}", new Object[] {
						commitsIdx, this.versions.get(versionsIdx).toString(), this.versions.get(versionsIdx+1).toString() 
				});
				
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
		int counter = 0;
		LocalDateTime date = (commitsIdx > 0) ? commits.get(commitsIdx-1).getDate() : null;

		Comparator<DatasetEntry> comparator = Comparator
				.comparing(DatasetEntry::getVersion)
				.thenComparing(DatasetEntry::getName);
		
		List<DatasetEntry> entries = new ArrayList<>(this.files.values());
		entries.sort(comparator);
		File dataset = new File(String.format("%s%s.csv", RESULTS_FOLDER, this.project));
		try (FileWriter writer = new FileWriter(dataset, true)) {
			for (DatasetEntry e : entries) {
				if (e.getSize() > 0) {
					counter++;
					writer.append(String.format("%s%n", e.toCSV(date)));
				}
			}
		}
		
		LOGGER.log(Level.INFO, "Evaluated statistics for {0} entries", counter);
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
