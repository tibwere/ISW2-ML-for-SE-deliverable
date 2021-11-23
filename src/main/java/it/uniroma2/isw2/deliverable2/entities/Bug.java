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
		
		/* Setting the key */
		bug.key = json.get("key").getAsString();
				
		JsonObject fields = json.get("fields").getAsJsonObject();
		Version v;
		
		/* Setting the FV */
		if (fields.get("fixVersions") == null)
			return null;	
		if ((v = extractFixVersion(fields.get("fixVersions").getAsJsonArray(), versions)) == null)
			return null;
		bug.fv = v;
			
		/* Setting OV*/
		if (fields.get("created") == null)
			return null;
		if ((v = extractOpenVersion(fields.get("created").getAsString(), versions)) == null)
			return null;
		bug.ov = v;
		if (bug.fv.getReleaseDate().isBefore(bug.ov.getReleaseDate()))
			return null;
		
		/* Setting AVs */
		bug.avs = extractAffectedVersions(fields.get("versions").getAsJsonArray(), versions);
		
		/* 
		 * Setting IV
		 * n.b. Since the version list received as parameter is sorted by ascending releaseDate 
		 * as IV I take the first AV
		 */
		if (!bug.avs.isEmpty())
			bug.iv = bug.avs.get(0);
		
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
	
	public Version getFv() {
		return fv;
	}

	public void setFv(Version fv) {
		this.fv = fv;
	}

	public Version getOv() {
		return ov;
	}

	public void setOv(Version ov) {
		this.ov = ov;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(this.key)
				.append(" [FV: ").append(this.fv.getName())
				.append(", OV: ").append(this.ov.getName());
				
		if (this.iv != null) {
			return sb.append(", IV:").append(this.iv.getName())
					.append(", sizeof(AV): ").append(this.avs.size())
					.append("]").toString();
		} else {
			return sb.append("]").toString();
		}
	}
}
