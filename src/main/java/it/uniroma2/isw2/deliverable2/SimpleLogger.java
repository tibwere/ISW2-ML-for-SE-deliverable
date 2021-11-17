package it.uniroma2.isw2.deliverable2;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleLogger {
	
	private SimpleLogger() {
		throw new IllegalStateException("Log class should not be instantiated");
	}
	
	private static final Logger LOGGER = Logger.getLogger("ISW2-DELIVERABLE-2");
	
	public static void logSevere(String message) {
		LOGGER.log(Level.SEVERE, message);
	}
	
	public static void logInfo(String message) {
		LOGGER.log(Level.INFO, message);
	}
	
	public static void logInfo(String fmt, Object []args) {
		LOGGER.log(Level.INFO, fmt, args);
	}
	
	public static void logInfo(String fmt, Object arg) {
		LOGGER.log(Level.INFO, fmt, arg);
	}
	
}
