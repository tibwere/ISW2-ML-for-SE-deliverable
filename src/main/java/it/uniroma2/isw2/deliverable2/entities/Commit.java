package it.uniroma2.isw2.deliverable2.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Commit {
	
	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	private String sha;
	private LocalDateTime date;
	private String message;
	private String author;
	
	private List<Diff> diffs;
	
	public Commit() {
		diffs = new ArrayList<>();
	}
	
	public String getSha() {
		return sha;
	}


	public void setSha(String sha) {
		this.sha = sha;
	}


	public LocalDateTime getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = LocalDateTime.parse(date, FORMAT);
	}


	public void setDate(LocalDateTime date) {
		this.date = date;
	}


	public String getAuthor() {
		return author;
	}


	public void setAuthor(String author) {
		this.author = author;
	}
	
	public void addDiff(Diff d) {
		this.diffs.add(d);
	}
	
	public List<Diff> getDiffs() {
		return this.diffs;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
