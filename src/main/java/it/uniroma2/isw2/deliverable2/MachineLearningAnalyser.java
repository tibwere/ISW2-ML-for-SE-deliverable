package it.uniroma2.isw2.deliverable2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class MachineLearningAnalyser {
	
	private static final Logger LOGGER = Logger.getLogger("ISW2-DELIVERABLE-2(ML)");

	private static final int VERSION_IDX = 0;
	
	private String projectName;
	private String resultsFolder;
	private List<String> versionNames;
	private Instances fullDataset;
	
	public MachineLearningAnalyser(String projectName, String resultsFolder) throws IOException {
		this.projectName = projectName;
		this.resultsFolder = resultsFolder;
		this.fullDataset = this.loadCSV(new File(String.format("%s%s_all_metrics.csv", this.resultsFolder, this.projectName)));
		this.versionNames = this.getVersionNames();
	}
	
	public void finalizeAnalysis() {
				
		for (int testingIdx=1; testingIdx<this.versionNames.size(); ++testingIdx) {
			Instances trainingSet = new Instances(this.fullDataset, 0);
			Instances testingSet = new Instances(this.fullDataset, 0);
			
			for (Instance row : fullDataset) {
				if (this.versionNames.indexOf(row.stringValue(VERSION_IDX)) == testingIdx)
					testingSet.add(row);
				if (this.versionNames.indexOf(row.stringValue(VERSION_IDX)) < testingIdx)
					trainingSet.add(row);
			}
			
			LOGGER.log(Level.INFO, "Training set size: {0} - Testing set size: {1}", new Object[] {
					trainingSet.size(), testingSet.size()
			});
		}
	}
	
	private Instances loadCSV(File csvFile) throws IOException {
		CSVLoader loader = new CSVLoader();
		loader.setSource(csvFile);
		return loader.getDataSet();
	}
	
	private List<String> getVersionNames() {
		List<String> versions = new ArrayList<>();
		
		for (Instance row : this.fullDataset)
			if (!versions.contains(row.stringValue(VERSION_IDX)))
				versions.add(row.stringValue(VERSION_IDX));
		
		return versions;
	}
}
