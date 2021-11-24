package it.uniroma2.isw2.deliverable2.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Bug {
	private String key;
	private Version fv;
	private Version ov;
	private Version iv;
	
	private Set<String> touchedFile;
	
	private Bug() {
		this.touchedFile = new HashSet<>();
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
	
	public String getKey() {
		return key;
	}

	public Version getIv() {
		return iv;
	}
	
	public void setIv(int proportion, List<Version> versions) {
		int fvIndex = versions.indexOf(this.fv);
		int ovIndex = versions.indexOf(this.ov);
		int ivIndex = fvIndex - (fvIndex - ovIndex)*proportion;
		
		this.iv = versions.get(ivIndex);
	}
	
	public boolean belongsTo(Version version) {
		if (this.iv == null) {
			return false;
		} else {
			return this.iv.getReleaseDate().isBefore(version.getReleaseDate())
					&& this.fv.getReleaseDate().isAfter(version.getReleaseDate());
		}
		
	}
	
	public double getProportion(List<Version> versions) {
		int fvIndex = versions.indexOf(this.fv);
		int ovIndex = versions.indexOf(this.ov);
		int ivIndex = versions.indexOf(this.iv);
		
		if (fvIndex == ovIndex)
			return ((double)fvIndex - ivIndex);
		else
			return ((double)(fvIndex - ivIndex)/(fvIndex - ovIndex));
	}
	
	public void addTouchedFile(String filename) {
		this.touchedFile.add(filename);
	}
	
	public boolean touches(String filename) {
		return this.touchedFile.contains(filename);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(this.key)
				.append(" [FV: ").append(this.fv.getName())
				.append(", OV: ").append(this.ov.getName());
				
		if (this.iv != null)
			return sb.append(", IV:").append(this.iv.getName()).append("]").toString();
		else
			return sb.append("]").toString();
	}
	
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
		
		/* Setting IV */
		bug.iv = extractInjectedVersion(fields.get("versions").getAsJsonArray(), versions);
		
		return bug; 
	}

	private static Version extractInjectedVersion(JsonArray jsonAvs, List<Version> versions) {		
		for (JsonElement element : jsonAvs) {
			if (element.getAsJsonObject().get("name") != null) {
				String versionName = element.getAsJsonObject().get("name").getAsString();
				
				for (Version v : versions)
					if (v.getName().equals(versionName))
						return v;
			}
		}
		return null;
	}
	
	private static Version extractFixVersion(JsonArray jsonFixVersions, List<Version> versions) {
		String fixVersion = null;
		LocalDateTime fixDate = null;
				
		for (JsonElement element : jsonFixVersions) {
			JsonObject jsonFixVersion = element.getAsJsonObject();
			if (missingFields(jsonFixVersion))
				continue;
						
			String version = jsonFixVersion.get("name").getAsString();
			LocalDateTime date = LocalDate.parse(jsonFixVersion.get("releaseDate").getAsString()).atStartOfDay();
			
			if (fixDate == null || date.isAfter(fixDate)) {
				fixVersion = version;
				fixDate = date;
			}
		}
				
		for (Version v : versions) 
			/* starts with is necessary because in STORM almost all bugs have a suffix in version name in JIRA */
			if (fixVersion != null && v.getName().startsWith(fixVersion))
				return v;
		
		return null;
	}
	
	private static boolean missingFields(JsonObject jsonFixVersion) {		
		return jsonFixVersion.get("name") == null || jsonFixVersion.get("name").getAsString() == null
				|| jsonFixVersion.get("releaseDate") == null || jsonFixVersion.get("releaseDate").getAsString() == null;
	}
	
	private static Version extractOpenVersion(String openDate, List<Version> versions) {
		for (Version v : versions)
			if (v.getReleaseDate().isAfter(LocalDate.parse(openDate.substring(0, 10)).atStartOfDay()))
				return v;
		return null;
	}
}
