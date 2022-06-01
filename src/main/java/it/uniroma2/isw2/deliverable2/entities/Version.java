package it.uniroma2.isw2.deliverable2.entities;

import java.time.LocalDateTime;

public class Version {
	private String name;
	private LocalDateTime releaseDate;

	public Version(String name, LocalDateTime startDate) {
		this.name = name;
		this.releaseDate = startDate;
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
