package main;

import htmlhandling.HTMLScraper;
import setup.PropertyManager;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


//Main control class for LabTracker program
//When ready for production, update Property file path in PropertyManager class
//and change log output path in log4j2.xml

public class PrintStatusExtractor {

	private static final Logger logger = LogManager.getLogger("PrintStatusExtractor");
	
	private static PropertyManager propManager = PropertyManager.getPropertyManagerInstance();

	public static void main(String[] args) throws IOException, SQLException, InterruptedException {

		logger.trace("*-----PrintStatusExtractor Is Starting!-----*");
		logger.trace("Loading Property Manager");
		propManager.loadProps();
		
		HTMLScraper scraper = new HTMLScraper();
		scraper.run();

		// Initiate HTMLScraper
		
		// Program End

	}
}
