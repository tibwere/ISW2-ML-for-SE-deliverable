package it.uniroma2.isw2.deliverable2.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalysisProfile {
	
	public static final String FEATURE_SELECTION_NO = "no selection";
	public static final String FEATURE_SELECTION_BEST_FIRST = "best first";
	public static final String SAMPLING_NO = "no sampling";
	public static final String SAMPLING_OVERSAMPLING = "oversampling";
	public static final String SAMPLING_UNDERSAMPLING = "undersampling";
	public static final String SAMPLING_SMOTE = "smote";
	public static final String COST_SENSITIVE_CLASSIFIER_NO = "no cost sensitive";
	public static final String COST_SENSITIVE_CLASSIFIER_SENSITIVE_THRESHOLD = "sensitive threshold";
	public static final String COST_SENSITIVE_CLASSIFIER_SENSITIVE_LEARNING = "sensitive learning";
	public static final String CLASSIFIER_RANDOM_FOREST = "random forest";
	public static final String CLASSIFIER_NAIVE_BAYES = "naive bayes";
	public static final String CLASSIFIER_IBK = "ibk";
	
	private String featureSelectionTechnique;
	private String samplingTechnique;
	private String costSensitiveTechnique;
	private String classifier;
	
	private AnalysisProfile(String featureSelection, String sampling, String costSensitiveClassifier, String classifier) throws WrongProfileException {
		this.setFeatureSelectionTechnique(featureSelection);
		this.setSamplingTechnique(sampling);
		this.setCostSensitiveTechnique(costSensitiveClassifier);
		this.setClassifier(classifier);
	}

	private void setFeatureSelectionTechnique(String featureSelectionTechnique) throws WrongProfileException {
		
		String []techniques = {
				FEATURE_SELECTION_NO, FEATURE_SELECTION_BEST_FIRST
		};
		
		if(Arrays.asList(techniques).contains(featureSelectionTechnique))
			this.featureSelectionTechnique = featureSelectionTechnique;
		else
			throw new WrongProfileException("Invalid feature selection techinque chosen");
	}

	private void setSamplingTechnique(String samplingTechnique) throws WrongProfileException {
		String []techniques = {
				SAMPLING_NO, SAMPLING_OVERSAMPLING, SAMPLING_UNDERSAMPLING, SAMPLING_SMOTE
		};
		
		if(Arrays.asList(techniques).contains(samplingTechnique))
			this.samplingTechnique = samplingTechnique;
		else
			throw new WrongProfileException("Invalid sampling techinque chosen");	
	}

	private void setCostSensitiveTechnique(String costSensitiveTechnique) throws WrongProfileException {
		String []techniques = {
				COST_SENSITIVE_CLASSIFIER_NO, COST_SENSITIVE_CLASSIFIER_SENSITIVE_LEARNING, COST_SENSITIVE_CLASSIFIER_SENSITIVE_THRESHOLD
		};
		
		if(Arrays.asList(techniques).contains(costSensitiveTechnique))
			this.costSensitiveTechnique = costSensitiveTechnique;
		else
			throw new WrongProfileException("Invalid cost sensitive techinque chosen");		
	}

	private void setClassifier(String classifier) throws WrongProfileException {
		String []techniques = {
				CLASSIFIER_RANDOM_FOREST, CLASSIFIER_NAIVE_BAYES, CLASSIFIER_IBK
		};
		
		if(Arrays.asList(techniques).contains(classifier))
			this.classifier = classifier;
		else
			throw new WrongProfileException("Invalid feature selection techinque chosen");		
	}
	
	public String getFeatureSelectionTechnique() {
		return featureSelectionTechnique;
	}

	public String getSamplingTechnique() {
		return samplingTechnique;
	}

	public String getCostSensitiveTechnique() {
		return costSensitiveTechnique;
	}

	public String getClassifier() {
		return classifier;
	}	
	
	public static List<AnalysisProfile> generateAllProfiles() throws WrongProfileException {
		String []costSensitiveClassifiers = {
				AnalysisProfile.COST_SENSITIVE_CLASSIFIER_NO,
				AnalysisProfile.COST_SENSITIVE_CLASSIFIER_SENSITIVE_LEARNING,
				AnalysisProfile.COST_SENSITIVE_CLASSIFIER_SENSITIVE_THRESHOLD
		};
		
		String []featureSelection = {
				AnalysisProfile.FEATURE_SELECTION_NO,
				AnalysisProfile.FEATURE_SELECTION_BEST_FIRST
		};
		
		String []sampling = {
				AnalysisProfile.SAMPLING_NO,
				AnalysisProfile.SAMPLING_OVERSAMPLING,
				AnalysisProfile.SAMPLING_UNDERSAMPLING,
				AnalysisProfile.SAMPLING_SMOTE
		};
		
		String []classifiers = {
				AnalysisProfile.CLASSIFIER_RANDOM_FOREST,
				AnalysisProfile.CLASSIFIER_NAIVE_BAYES,
				AnalysisProfile.CLASSIFIER_IBK
		};
		
		List<AnalysisProfile> profiles = new ArrayList<>();
		
		for (String fs : featureSelection)
			for (String s : sampling)
//				for (String csc : costSensitiveClassifiers)
					for (String c : classifiers)
						profiles.add(new AnalysisProfile(fs, s, COST_SENSITIVE_CLASSIFIER_NO, c));
		
		return profiles;
	}
}
