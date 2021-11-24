package it.uniroma2.isw2.deliverable2;

import java.io.IOException;

public class Launcher {
	
	private static final String PROJECT_NAME = "STORM";
	
	public static void main(String[] args) throws IOException {
		new Analyser(PROJECT_NAME).run();
	}
}
