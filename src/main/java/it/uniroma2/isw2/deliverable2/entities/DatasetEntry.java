package it.uniroma2.isw2.deliverable2.entities;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class DatasetEntry {
	
	public static final String CSV_HEADER = "Version,Name,Size,NR,NAuth,LOC added,MAX LOC added,AVG LOC added,Churn,MAX Churn,AVG Churn,ChgSetSize,Age\n";
	
	private LocalDateTime birth;
	private String version;
	private String name;
	private int size;
	private int additions;
	private int deletions;
	private int maxChurn;
	private	int maxAddition;
	private int updateTimes;
	private List<Commit> commits;
	
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
		commits = new ArrayList<>();
	}
	
	public DatasetEntry(String version, String name, LocalDateTime birth, int initialSize) {
		this(version, name, birth);
		this.size = initialSize;
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
		if (!commits.contains(commit))
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
		return ((double)this.additions/this.updateTimes);
	}
	
	private int churn() {
		return this.additions - this.deletions;
	}
	
	private double avgChurn() {
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
	
	public String toCSV(LocalDateTime currentDate) {
		long age = (currentDate == null) ? 0 : ChronoUnit.WEEKS.between(this.birth, currentDate);

		return String.format("%s,%s,%d,%d,%d,%d,%d,%.3f,%d,%.3f,%d,%d", 
				this.version, this.name, this.size, this.numberOfRevisions(),
				this.numberOfAuthors(), this.additions, this.maxAddition,this.avgAdditions(),
				this.churn(), this.avgChurn(), this.chgSetSize(),age
		);
	}
}
