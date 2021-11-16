package it.uniroma2.isw2.deliverable2.entities;

import java.time.LocalDateTime;

public class Version {
	private String id;
	private String name;
	private LocalDateTime date;
	
	public Version(String id, String name, LocalDateTime date) {
		this.id = id;
		this.name = name;
		this.date = date;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public LocalDateTime getDate() {
		return date;
	}
	
	@Override
	public String toString() {
		return new StringBuilder(this.name)
				.append("(").append(this.id).append(")")
				.append(" [").append(this.date).append("]")
				.toString();
	}
}
