package it.uniroma2.isw2.deliverable2;

public class Launcher {
	
	private static final String PROJECT_NAME = "BOOKKEEPER";
	
	public static void main(String[] args) throws Exception {
		new Analyser(PROJECT_NAME).run();
	}
}
