package it.uniroma2.isw2.deliverable2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
		File resultFolder = new File(RESULTS_FOLDER);
		if (!resultFolder.exists())
			resultFolder.mkdir();
	}

	private void evalStatistics(List<DatasetEntry> entries) throws IOException {
		
		Comparator<DatasetEntry> comparator = Comparator
				.comparing(DatasetEntry::getVersion)
				.thenComparing(DatasetEntry::getName);
		
		entries.sort(comparator);
		File dataset = new File(String.format("%s%s.csv", RESULTS_FOLDER, this.project));
		try (FileWriter writer = new FileWriter(dataset, false)) {
			writer.append(DatasetEntry.CSV_HEADER);
			for (DatasetEntry e : entries) 
				writer.append(String.format("%s%n", e.toString()));
		}
	}

	public void createDataset() throws IOException {
		Iterator<Version> versions = jira.getVersions().iterator();
		Iterator<String> shas = git.getCommitsSHA().iterator();
		Map<String, DatasetEntry> files = new HashMap<>();
		Version currentVersion = null;

		if (versions.hasNext())
			currentVersion = versions.next();
		else
			return;

		while (shas.hasNext()) {
			String currentSha = shas.next();
			Commit currentCommit = git.getCommit(currentSha);

			if (currentCommit.getDate().isAfter(currentVersion.getEndDate())) {
				this.evalStatistics(new ArrayList<>(files.values()));
				if (versions.hasNext())
					currentVersion = versions.next();
				else
					return;
			}
				
			for (Diff d : currentCommit.getDiffs()) {
				DatasetEntry entry = null;
				String key = d.getFilename();

				if (files.containsKey(key)) {
					entry = files.get(key);
				} else {
					entry = new DatasetEntry(currentVersion.getName(), key);
				}

				entry.incAddition(d.getAdditions());
				entry.incDeletions(d.getDeletions());
				entry.insertAuthor(currentCommit.getAuthor());

				files.put(key, entry);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		Analyser analyser = new Analyser(PROJECT_NAME);
		analyser.createDataset();
	}
}