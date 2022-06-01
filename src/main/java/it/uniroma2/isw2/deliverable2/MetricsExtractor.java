package it.uniroma2.isw2.deliverable2;

import it.uniroma2.isw2.deliverable2.entities.*;
import it.uniroma2.isw2.deliverable2.exceptions.MaximumRequestToGithubAPIException;
import it.uniroma2.isw2.deliverable2.exceptions.MissingGithubTokenException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetricsExtractor {
	
	private static final Logger LOGGER = Logger.getLogger("ISW2-DELIVERABLE-2(EX)");
	
	private String project;
	
	private GitHelper gitHelper;
	private JIRAHelper jiraHelper;
	
	private List<Version> versions;
	private List<Commit> commits;
	private List<Bug> bugs;
	private Map<String, VersionedFile> files;
	private String resultsFolder;
		
	public MetricsExtractor(String project, String resultsFolder) throws MissingGithubTokenException {
		this.project = project;
		this.gitHelper = new GitHelper(this.project);
		this.jiraHelper = new JIRAHelper(this.project);
		this.resultsFolder = resultsFolder;
	}
	
	public void extract() throws IOException, MaximumRequestToGithubAPIException {
		/* Init attributes */
		int targetVersionIdx = this.retrieveInformation();
		
		/* Fill IV of bugs using proportion */
		this.fillIVs();
		
		/* Prepare the set of touched files for each bug */
		this.fillTouchedFiles();
				
		/* Create dataset */
		this.createDataset(targetVersionIdx);	
		
		/* Create csv file */
		this.dumpStatisticsOnCSVFile();
	}
	
	private int retrieveInformation() throws IOException, MaximumRequestToGithubAPIException {
		LOGGER.log(Level.INFO, "*** Retrieve tickets from JIRA ***");
		this.versions = jiraHelper.getVersions();
		LOGGER.log(Level.INFO, "Found {0} versions", this.versions.size());
						
		LOGGER.log(Level.INFO, "*** Retrieve bugs from JIRA ***");
		this.bugs = jiraHelper.getBugs(this.versions);
		LOGGER.log(Level.INFO, "*** Found {0} bugs", this.bugs.size());
		
		/* Sort bugs for OV */
		this.bugs.sort((b1, b2) -> b1.getOv().getReleaseDate().compareTo(b2.getOv().getReleaseDate()));
		
		int targetIdx = (int)Math.ceil(this.versions.size()/2.0);
		LocalDateTime lastBugFixVersionReleaseDate = this.bugs.get(this.bugs.size()-1).getFv().getReleaseDate();
		LocalDateTime lastConsideredVersionReleaseDate = this.versions.get(targetIdx).getReleaseDate();
		
		LOGGER.log(Level.INFO, "*** Retrieve commits from Github ***");
		this.commits = gitHelper.getCommits(getMaxDate(lastBugFixVersionReleaseDate, lastConsideredVersionReleaseDate));
		LOGGER.log(Level.INFO, "*** Found {0} commits", this.commits.size());

		this.files = new HashMap<>();
		
		LOGGER.log(Level.INFO, "Considering only first half of versions ({0})", targetIdx);
		return targetIdx;
	}
	
	private void fillIVs() {
		for (Bug b : this.bugs) {
			if (b.getIv() == null)
				b.setIv(this.bugs, this.versions);
		}
	}
	
	private void fillTouchedFiles() {
		for (Bug bug : this.bugs) {		
			for (Commit commit : this.commits) {
				/* It's possible that more than one commit is used to fix a bug */
				if (commit.getMessage().contains(bug.getKey())) {
					for (Diff d : commit.getDiffs())
						bug.addTouchedFile(d.getFilename());
				}
			}
		}
	}
	
	private void createDataset(int targetVersionIndex) {
		int versionsIdx = 0;
		int commitsIdx = 0;
				
		while (versionsIdx < targetVersionIndex) {			
			if (this.needSwitchVersion(versionsIdx, commitsIdx)) {
				LOGGER.log(Level.INFO, "After {0} commits switch from version {1} to {2}", new Object[] {
						commitsIdx, this.versions.get(versionsIdx), this.versions.get(versionsIdx+1)
				});
				
				this.evalStatistics(commitsIdx, versionsIdx++);
			}
			
			this.applyDiff(commitsIdx++);
		}
	}
	
	
	private void dumpStatisticsOnCSVFile() throws IOException {
		List<Metrics> metrics = new ArrayList<>();
		for (VersionedFile f : this.files.values())
			metrics.addAll(f.getComputedMetrics());
		
		/* First, order by release date of version ...*/
		Comparator<Metrics> comparator = Comparator.comparing(m -> m.getVersion().getReleaseDate());
		/* ... then order by name */
		comparator = comparator.thenComparing(Comparator.comparing(Metrics::getName));
		
		metrics.sort(comparator);
		
		File csvDataset = new File(String.format("%s%s_all_metrics.csv", this.resultsFolder, this.project));

		try (FileWriter writer = new FileWriter(csvDataset, false)) {
			writer.append(Metrics.CSV_HEADER);
			for (Metrics m : metrics)
				writer.append(String.format("%s%n", m));
		}
		LOGGER.log(Level.INFO, "Dumped dataset on CSV file");
	}
	
	private LocalDateTime getMaxDate(LocalDateTime d1, LocalDateTime d2) {
		return (d1.isAfter(d2)) ? d1 : d2;
	}
	
	private void applyDiff(int commitsIdx) {
		for (Diff d : this.commits.get(commitsIdx).getDiffs()) {
			String key = d.getFilename();
			
			VersionedFile entry = null;
			if (this.files.containsKey(key))
				entry = this.files.get(key);
			else
				entry = new VersionedFile(d.getFilename(), commits.get(commitsIdx).getDate());
			
			entry.updateChurn(d.getAdditions(), d.getDeletions());
			entry.insertCommit(this.commits.get(commitsIdx));

			this.files.put(key, entry);
		}
	}
	
	private boolean needSwitchVersion(int versionsIdx, int commitsIdx) {
		return this.commits.get(commitsIdx).getDate().isAfter(this.versions.get(versionsIdx+1).getReleaseDate());
	}
	
	private void evalStatistics(int commitsIdx, int versionIdx) {
		LocalDateTime date = (commitsIdx > 0) ? commits.get(commitsIdx-1).getDate() : null;

		for (VersionedFile f : this.files.values()) 
			f.computeMetrics(this.versions.get(versionIdx), date, this.bugs);
		
		LOGGER.log(Level.INFO, "Evaluated statistics for {0} entries", this.files.values().size());
	}	
}
