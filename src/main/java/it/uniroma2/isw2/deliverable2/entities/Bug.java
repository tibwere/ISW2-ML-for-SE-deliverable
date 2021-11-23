package it.uniroma2.isw2.deliverable2.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Bug {
	private String key;
	private Version fv;
	private Version ov;
	private Version iv;
	private List<Version> avs;
	
	private Bug() {/* To be instantiated via static method */}
	
	public static Bug fromJsonObject(JsonObject json, List<Version> versions) {
		Bug bug = new Bug();
		if (json.get("key") == null)
			return null;
		
		bug.key = json.get("key").getAsString();
				
		JsonObject fields = json.get("fields").getAsJsonObject();
		Version v;
		
		if (fields.get("fixVersions") == null)
			return null;	
		if ((v = extractFixVersion(fields.get("fixVersions").getAsJsonArray(), versions)) == null)
			return null;
		bug.fv = v;
				
		if (fields.get("created") == null)
			return null;
		if ((v = extractOpenVersion(fields.get("created").getAsString(), versions)) == null)
			return null;
		
		bug.ov = v;
		bug.avs = extractAffectedVersions(fields.get("versions").getAsJsonArray(), versions);
		
		return bug; 
	}

	private static List<Version> extractAffectedVersions(JsonArray jsonAvs, List<Version> versions) {
		List<Version> avs = new ArrayList<>();
		
		for (JsonElement element : jsonAvs) {
			if (element.getAsJsonObject().get("name") != null) {
				String versionName = element.getAsJsonObject().get("name").getAsString();
				
				for (Version v : versions)
					if (v.getName().equals(versionName))
						avs.add(v);
			}
		}
		
		return avs;
	}
	
	private static Version extractFixVersion(JsonArray jsonFixVersions, List<Version> versions) {
		String fixVersion = null;
		LocalDateTime fixDate = null;
				
		for (JsonElement element : jsonFixVersions) {
			JsonObject jsonFixVersion = element.getAsJsonObject();
			if (jsonFixVersion.get("name").getAsString() == null || jsonFixVersion.get("releaseDate").getAsString() == null)
				continue;
			
			String version = jsonFixVersion.get("name").getAsString();
			LocalDateTime date = LocalDate.parse(jsonFixVersion.get("releaseDate").getAsString()).atStartOfDay();
			
		
			if (fixDate == null || date.isAfter(fixDate)) {
				fixVersion = version;
				fixDate = date;
			}
		}
				
		for (Version v : versions) 
			if (v.getName().equals(fixVersion))
				return v;
		
		return null;
	}
	
	private static Version extractOpenVersion(String openDate, List<Version> versions) {
				
		for (Version v : versions)
			if (v.getReleaseDate().isAfter(LocalDate.parse(openDate.substring(0, 10)).atStartOfDay()))
				return v;
		return null;
	}
	
	public String toString() {
		return new StringBuilder(this.key).append("FV: ").append(this.fv.getName())
				.append(", OV: ").append(this.ov.getName())
				.append(", size(AV) = ").append(this.avs.size())
				.toString();
	}
}
