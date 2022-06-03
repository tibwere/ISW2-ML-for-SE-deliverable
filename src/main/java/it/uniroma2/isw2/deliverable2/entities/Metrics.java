package it.uniroma2.isw2.deliverable2.entities;

public class Metrics {

	public static final String ARFF_HEADER_FMT = "@relation %s\n" +
			"\n" +
			"@attribute Version string\n" +
			"@attribute Name string\n" +
			"@attribute Size numeric\n" +
			"@attribute NR numeric\n" +
			"@attribute NAuth numeric\n" +
			"@attribute 'LOC added' numeric\n" +
			"@attribute 'MAX LOC added' numeric\n" +
			"@attribute 'AVG LOC added' numeric\n" +
			"@attribute Churn numeric\n" +
			"@attribute 'MAX Churn' numeric\n" +
			"@attribute 'AVG Churn' numeric\n" +
			"@attribute ChgSetSize numeric\n" +
			"@attribute Age numeric\n" +
			"@attribute Buggyness {N,Y}\n" +
			"\n" +
			"@data" +
			"\n";
	
	public static final String CSV_HEADER = "Version,Name,Size,NR,NAuth,LOC added,MAX LOC added,AVG LOC added,Churn,MAX Churn,AVG Churn,ChgSetSize,Age,Buggyness\n";
	
	private Version version;
	private String name;
	private int size;
	private int numberOfRevisions;
	private int numberOfAuthors;
	private int locAdded;
	private int maxLOCAdded;
	private double averageLOCAdded;
	private int churn;
	private int maxChurn;
	private double averageChurn;
	private int changeSetSize;
	private long age;
	private char buggyness;
		
	@Override
	public String toString() {
		return String.format("%s,%s,%d,%d,%d,%d,%d,%.7f,%d,%d,%.7f,%d,%d,%s",
				this.version.getName(), this.name, this.size, this.numberOfRevisions,
				this.numberOfAuthors, this.locAdded, this.maxLOCAdded,this.averageLOCAdded,
				this.churn, this.maxChurn, this.averageChurn, this.changeSetSize, this.age, this.buggyness
		);
	}

	public void setVersion(Version version) {
		this.version = version;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void setNumberOfRevisions(int numberOfRevisions) {
		this.numberOfRevisions = numberOfRevisions;
	}

	public void setNumberOfAuthors(int numberOfAuthors) {
		this.numberOfAuthors = numberOfAuthors;
	}

	public void setLOCAdded(int locAdded) {
		this.locAdded = locAdded;
	}

	public void setMaxLOCAdded(int maxLOCAdded) {
		this.maxLOCAdded = maxLOCAdded;
	}

	public void setAverageLOCAdded(double averageLOCAdded) {
		this.averageLOCAdded = averageLOCAdded;
	}

	public void setChurn(int churn) {
		this.churn = churn;
	}

	public void setMaxChurn(int maxChurn) {
		this.maxChurn = maxChurn;
	}

	public void setAverageChurn(double averageChurn) {
		this.averageChurn = averageChurn;
	}

	public void setChangeSetSize(int changeSetSize) {
		this.changeSetSize = changeSetSize;
	}

	public void setAge(long age) {
		this.age = age;
	}

	public void setBuggyness(boolean buggyness) {
		this.buggyness = (buggyness) ? 'Y' : 'N';
	}

	public Version getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}
}
