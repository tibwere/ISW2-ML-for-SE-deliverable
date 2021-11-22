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

import it.uniroma2.isw2.deliverable2.entities.Commit;
import it.uniroma2.isw2.deliverable2.entities.DatasetEntry;
import it.uniroma2.isw2.deliverable2.entities.Diff;
import it.uniroma2.isw2.deliverable2.entities.Version;

public class Analyser {

	private static final String RESULTS_FOLDER = "results/";
	private static final String PROJECT_NAME = "STORM";
	
	private JIRAHelper jira;
	private GitHelper git;
	private String project;

	public Analyser(String project) throws IOException {
		this.jira = new JIRAHelper(project);
		this.git = new GitHelper(project);
		this.project = project;
		
		setupResultsFolder();
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

	private void evalStatistics(List<DatasetEntry> entries, LocalDateTime date) throws IOException {
		Comparator<DatasetEntry> comparator = Comparator
				.comparing(DatasetEntry::getVersion)
				.thenComparing(DatasetEntry::getName);
		
		entries.sort(comparator);
		File dataset = new File(String.format("%s%s.csv", RESULTS_FOLDER, this.project));
		try (FileWriter writer = new FileWriter(dataset, true)) {
			for (DatasetEntry e : entries) {
				if (e.getSize() > 0)
					writer.append(String.format("%s%n", e.toCSV(date)));
			}
		}
	}
	
	private Map<String, DatasetEntry> resetFilesForNewVersion(Map<String, DatasetEntry> files, String version) {
		Map<String, DatasetEntry> newEntries = new HashMap<>();
		
		for (DatasetEntry entry : files.values()) {
			DatasetEntry newEntry = new DatasetEntry(version, entry.getName(), entry.getBirth(), entry.getSize());
			newEntries.put(entry.getName(), newEntry);
		}
		
		return newEntries;
	}
	
	private boolean needSwitchVersion(Version version, Commit commit) {
		return commit.getDate().isAfter(version.getEndDate());
	}
	
	private void applyDiff(Version currentVersion, Commit currentCommit, Map<String, DatasetEntry> files) {
		for (Diff d : currentCommit.getDiffs()) {
			String key = d.getFilename();
			DatasetEntry entry = (files.containsKey(key)) ? files.get(key) : new DatasetEntry(currentVersion.getName(), d.getFilename(), currentCommit.getDate());
			
			entry.updateChurn(d.getAdditions(), d.getDeletions());
			entry.insertCommit(currentCommit);

			files.put(key, entry);
		}
	}
	
	public void createDataset() throws IOException {
		List<Version> versions = jira.getVersions();
		int versionsIdx = 0;
		
		LocalDateTime targetDate = versions.get(versions.size()-1).getEndDate();
		
		List<Commit> commits = git.getCommits(targetDate);
		int commitsIdx = 0;
		
		Map<String, DatasetEntry> files = new HashMap<>();
		
		while (versionsIdx < versions.size() && commitsIdx < commits.size()) {			
			if (needSwitchVersion(versions.get(versionsIdx), commits.get(commitsIdx))) {
				LocalDateTime date = (commitsIdx > 0) ? commits.get(commitsIdx-1).getDate() : null;
				this.evalStatistics(new ArrayList<>(files.values()), date);
				files = resetFilesForNewVersion(files, versions.get(++versionsIdx).getName());
			}
			
			applyDiff(versions.get(versionsIdx), commits.get(commitsIdx++), files);
		}
		
		// Dump pending commits
		LocalDateTime date = (commitsIdx > 0) ? commits.get(commitsIdx-1).getDate() : null;
		this.evalStatistics(new ArrayList<>(files.values()), date);
	}

	public static void main(String[] args) throws IOException {
		Analyser analyser = new Analyser(PROJECT_NAME);
		analyser.createDataset();
	}
}