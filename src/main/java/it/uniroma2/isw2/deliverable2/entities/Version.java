package it.uniroma2.isw2.deliverable2.entities;

import java.time.LocalDateTime;

public class Version {
	private String id;
	private String name;
	private LocalDateTime startDate;
	private LocalDateTime endDate;

	public Version(String id, String name, LocalDateTime startDate) {
		this.id = id;
		this.name = name;
		this.startDate = startDate;
	}

	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	@Override
	public String toString() {
		return new StringBuilder(this.name).append("(").append(this.id).append(")").append(" [from: ")
				.append(this.startDate).append(" to: ").append(this.endDate).append("]").toString();
	}
}
