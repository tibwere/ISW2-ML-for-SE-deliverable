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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.uniroma2.isw2.deliverable2.entities.Commit;
import it.uniroma2.isw2.deliverable2.entities.DatasetEntry;
import it.uniroma2.isw2.deliverable2.entities.Diff;
import it.uniroma2.isw2.deliverable2.entities.Version;

public class Analyser {

	private static final String RESULTS_FOLDER = "results/";
	private static final String PROJECT_NAME = "BOOKKEEPER";
	
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

	public void createDataset() throws IOException {
		Iterator<Version> versions = jira.getVersions().iterator();
		Iterator<String> shas = git.getCommitsSHA().iterator();
		Map<String, DatasetEntry> files = new HashMap<>();
		Version currentVersion = null;
		Commit previousCommit = null;
		Commit currentCommit = null;

		if (versions.hasNext())
			currentVersion = versions.next();
		else
			return;

		while (shas.hasNext()) {
			String currentSha = shas.next();
			previousCommit = currentCommit;
			currentCommit = git.getCommit(currentSha);

			if (currentCommit.getDate().isAfter(currentVersion.getEndDate())) {
				this.evalStatistics(new ArrayList<>(files.values()), (previousCommit == null) ? null : previousCommit.getDate());
				if (versions.hasNext()) {
					currentVersion = versions.next();
					files = resetFilesForNewVersion(files, currentVersion.getName());
				} else {
					return;
				}
			}
				
			for (Diff d : currentCommit.getDiffs()) {
				DatasetEntry entry = null;
				String key = d.getFilename();

				if (files.containsKey(key))
					entry = files.get(key);
				else
					entry = new DatasetEntry(currentVersion.getName(), d.getFilename(), currentCommit.getDate());

				entry.updateChurn(d.getAdditions(), d.getDeletions());
				entry.insertCommit(currentCommit);

				files.put(key, entry);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		Analyser analyser = new Analyser(PROJECT_NAME);
		analyser.createDataset();
	}
}