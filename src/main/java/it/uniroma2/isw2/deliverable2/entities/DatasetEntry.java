package it.uniroma2.isw2.deliverable2.entities;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatasetEntry {
	
	public static final String CSV_HEADER = "Version,Name,Size,NR,NAuth,LOC added,MAX LOC added,AVG LOC added,Churn,MAX Churn,AVG Churn,ChgSetSize,Age,Buggyness\n";
	
	private LocalDateTime birth;
	private String version;
	private String name;
	private int size;
	private int additions;
	private int deletions;
	private int maxChurn;
	private	int maxAddition;
	private int updateTimes;
	private Set<Commit> commits;
	private boolean buggyness;
	
	public DatasetEntry(String version, String name, LocalDateTime birth) {
		this.version = version;
		this.name = name;
		this.birth = birth;
		this.size = 0;
		this.additions = 0;
		this.deletions = 0;
		this.maxChurn=0;
		this.maxAddition = 0;
		this.updateTimes = 0;
		this.commits = new HashSet<>();
	}
	
	public int getSize() {
		return size;
	}

	public String getVersion() {
		return version;
	}
	
	public String getName() {
		return name;
	}
	
	public LocalDateTime getBirth() {
		return birth;
	}
	
	public void updateChurn(int additions, int deletions) {
		if (additions > this.maxAddition)
			this.maxAddition = additions;
		
		this.updateTimes++;
		this.additions += additions;
		this.deletions += deletions;
		
		int churn = additions - deletions;
		if (churn > this.maxChurn)
			this.maxChurn = churn;
		this.size += churn;
	}
	
	public void insertCommit(Commit commit) {
			commits.add(commit);
	}
	
	private int numberOfAuthors() {
		List<String> authors = new ArrayList<>();
		this.commits.forEach(c->{
			if (!authors.contains(c.getAuthor()))
				authors.add(c.getAuthor());
		});
		
		return authors.size();
	}
	
	private int numberOfRevisions() {
		return commits.size();
	}
	
	private double avgAdditions() {
		if (this.updateTimes == 0)
			return 0;
		
		return ((double)this.additions/this.updateTimes);
	}
	
	private int churn() {
		return this.additions - this.deletions;
	}
	
	private double avgChurn() {
		if (this.updateTimes == 0)
			return 0;
		
		return ((double)this.churn()/this.updateTimes);
	}
	
	private int chgSetSize() {
		List<String> files = new ArrayList<>();
		
		this.commits.forEach(c-> {
			List<Diff> fileChanged = c.getDiffs();
			fileChanged.forEach(d-> {
				if(!files.contains(d.getFilename()))
					files.add(d.getFilename());
			});
		});
		
		return files.size();
	}
	
	public void setBuggyness(boolean buggynes) {
		this.buggyness = buggynes;
	}
	
	public String toCSV(LocalDateTime currentDate) {
		long age = (currentDate == null) ? 0 : ChronoUnit.WEEKS.between(this.birth, currentDate);
		String buggynessStr = (this.buggyness) ? "Y" : "N";

		return String.format("%s,%s,%d,%d,%d,%d,%d,%.3f,%d,%d,%.3f,%d,%d,%s", 
				this.version, this.name, this.size, this.numberOfRevisions(),
				this.numberOfAuthors(), this.additions, this.maxAddition,this.avgAdditions(),
				this.churn(), this.maxChurn, this.avgChurn(), this.chgSetSize(),age,buggynessStr
		);
	}
	
	public static DatasetEntry fromPrevious(DatasetEntry oldEntry, String currentVersionName) {
		DatasetEntry newEntry = new DatasetEntry(currentVersionName, oldEntry.getName(), oldEntry.getBirth());
		newEntry.size = oldEntry.getSize();
		return newEntry;				
	}
}
