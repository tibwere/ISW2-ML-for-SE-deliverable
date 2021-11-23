package it.uniroma2.isw2.deliverable2.entities;

import java.time.LocalDateTime;

public class Version {
	private String id;
	private String name;
	private LocalDateTime releaseDate;

	public Version(String id, String name, LocalDateTime startDate) {
		this.id = id;
		this.name = name;
		this.releaseDate = startDate;
	}

	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public LocalDateTime getReleaseDate() {
		return releaseDate;
	}

	@Override
	public String toString() {
		return new StringBuilder(this.name).append(" (").append(this.releaseDate).append(")").toString();
	}
}
