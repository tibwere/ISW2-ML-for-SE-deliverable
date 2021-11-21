package it.uniroma2.isw2.deliverable2.entities;

import java.util.ArrayList;
import java.util.List;

public class DatasetEntry {
	
	public static final String CSV_HEADER = "Version,Name,Size\n";
	
	private String version;
	private String name;
	private int size;
	private int additions;
	private int deletions;
	private List<String> authors;
	
	public DatasetEntry(String version, String name) {
		this.version = version;
		this.name = name;
		authors = new ArrayList<>();
	}
	
	public int getAdditions() {
		return additions;
	}

	public int getDeletions() {
		return deletions;
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}

	public String getVersion() {
		return version;
	}
	
	public String getName() {
		return name;
	}
	
	public void incAddition(int add) {
		this.additions += add;
		this.size += add;
	}
	
	public void incDeletions(int del) {
		this.deletions += del;
		this.size -= del;
	}
	
	public void insertAuthor(String author) {
		if (!authors.contains(author))
			authors.add(author);
	}
	
	public int numberOfAuthors() {
		return authors.size();
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder()
				.append(version).append(",")
				.append(name).append(",")
				.append(size);
		
		return sb.toString();
	}
}
