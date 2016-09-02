package htmlhandling;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.LabTracker;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import setup.PropertyManager;
import dataobjects.Printer;
import dataobjects.Tray;
import db.DBConnector;

/**
 * Created by Jordi Diaz on 12/22/14. Need to implement ability to read multiple
 * files and delete upon completion, no hard coding. Export ArrayList to local
 * file or DB.
 */
public class HTMLParser {

	private Printer currentLab;

	private Map<String, String> parserProperties = new HashMap<String, String>();
	private Map<String, String> suppressionProperties = new HashMap<String, String>();

	private String parserSuppressionFilePath = null;
	private Integer parserReportingThreshold = 0;

	private Integer numInUse = 0;
	private Integer numAvail = 0;
	private Integer numOffline = 0;

	private Elements stationNameDivs;
	private Elements statusDivs;
	private Elements osImageDivs;

	private ArrayList<Tray> stations = new ArrayList<Tray>();
	private static String errorInfo;

	private DBConnector dbConnector = new DBConnector();
	private HTMLCreator htmlCreator = new HTMLCreator();

	private static final Logger logger = LogManager.getLogger("LabTracker");

	public void run(Printer currentLab) throws IOException, SQLException {
		logger.trace("*-----HTMLParser Is Starting!-----*");
		this.currentLab = currentLab;

		logger.trace("Retrieving Parser Properties");
		getProps();

		logger.trace("Parsing HTML For Requested Data");
		parseHTML();

		logger.trace("Creating Station Objects");
		createStationObjects();
		setCountVariables();

		logger.trace("Setting Suppressed Stations");
		setSuppressedStations();

		LabTracker.addLab(currentLab);

		logger.trace("Checking Error Reporting Threshold");
		detectDataThreshold();

		logger.trace("Writing Data To MySQL DB");
		dbConnector.createConnection();
		dbConnector.writeToLabTable(currentLab);
		dbConnector.writeToRunStatusTable(currentLab);
		dbConnector.writeToFlatTable(currentLab);
		dbConnector.closeConnection();

		logger.trace("Creating HTML Map Page");
		htmlCreator.setLab(currentLab);
		htmlCreator.getProps();
		htmlCreator.writeMapOfStationsToHTML();
	}

	private void getProps() throws IOException {
		PropertyManager propManager = PropertyManager
				.getPropertyManagerInstance();
		this.parserProperties = propManager.getParserProperties();
		this.suppressionProperties = propManager.getSuppressionProperties();
		this.parserSuppressionFilePath = parserProperties
				.get("parserSuppressionFilePath");
		this.parserReportingThreshold = Integer.parseInt(parserProperties
				.get("parserReportingThreshold"));
		logger.trace("Supression File Path:          "
				+ parserSuppressionFilePath);
		logger.trace("Parser Reporting Threshold:    "
				+ parserReportingThreshold + "%");
	}

	/**
	 * Methods to extract needed fields from HTML These methods are not really
	 * needed, but structure of HTML may change in the future. May be beneficial
	 * to make code as modular as possible to avoid coding confusion later
	 */
	private void parseHTML() throws IOException {
		/**
		 * Load HTML pulled from page into File then load file into Document for
		 * parsing
		 */
		Document doc = Jsoup.parse(currentLab.getScrapedHTML());
		// Create elements out of relevant HTML divs
		stationNameDivs = doc.getElementsByClass("station-label");
		statusDivs = doc.getElementsByClass("station");
		osImageDivs = doc.getElementsByClass("os-image");
	}

	/**
	 * Iterate through station divs, get relevant information out, then turn
	 * them into stations and/or store all retrieved data in some sort of
	 * persistent/saved data structure
	 */
	private void createStationObjects() {
		// Iterates through divs containing station ID info
		for (int k = 0; k < stationNameDivs.size(); k++) {
			String stationName = stationNameDivs.get(k).text();
			String stationID = statusDivs.get(k).id();
			String status = getStationStatus(statusDivs.get(k).toString());
			String OS = getStationOS(osImageDivs.get(k).toString());
			Tray stu1 = new Tray(stationName, stationID,
					status, OS);
			stations.add(k, stu1);
		}
		currentLab.setStations(stations);
	}

	private String getStationStatus(String statusDiv) {
		Pattern pat = Pattern.compile("(?<=Computer-01-)(\\w*)(?=\\.png)");
		Matcher mat = pat.matcher(statusDiv);
		String stationStatus;
		if (mat.find()) {
			stationStatus = mat.group().toString();
			if (stationStatus.matches("InUse")) {
				stationStatus = "InUse";
			} else if (stationStatus.matches("PoweredOn")) {
				stationStatus = "Available";
			}
		} else {
			stationStatus = "Offline";
		}
		return stationStatus;
	}

	private String getStationOS(String osDiv) {
		Pattern pat = Pattern.compile("(?<=/)(\\w*)(?=\\.png)");
		Matcher mat = pat.matcher(osDiv);
		String stationOS;
		if (mat.find()) {
			stationOS = mat.group().toString();
		} else {
			stationOS = "N/A";
		}
		return stationOS;
	}

	private void setCountVariables() {
		for (Tray station : stations) {
			if (station.getStationStatus().matches("Available")) {
				numAvail++;
			} else if (station.getStationStatus().matches("InUse")) {
				numInUse++;
			} else if (station.getStationStatus().matches("Offline")) {
				numOffline++;
			}
		}
		currentLab.setInUse(numInUse);
		currentLab.setAvail(numAvail);
		currentLab.setOffline(numOffline);
		currentLab.setTotalInternally();
		logger.trace("Number of Available: " + numAvail);
		logger.trace("Number of In Use: " + numInUse);
		logger.trace("Number of Offline: " + numOffline);
		logger.trace("Total Number of Units: " + currentLab.getTotal());
	}

	@SuppressWarnings("rawtypes")
	private void setSuppressedStations() {
		int numSup = 0;
		for (int i = 0; i < stations.size(); i++) {
			Iterator<?> it = suppressionProperties.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				if (stations.get(i).getStationName()
						.matches(pair.getValue().toString())) {
					stations.get(i).setStationStatus("Suppressed");
					numSup += 1;
				}
			}
		}
		currentLab.setSuppressed(numSup);
		currentLab.setTotal(currentLab.getTotal() - currentLab.getSuppressed());
	}

	private void detectDataThreshold() {
		// check if num of stations offline/ not reporting is above acceptable
		// threshold
		double percentThreshold = (double) parserReportingThreshold / 100;
		double percentOffline = (double) numOffline / currentLab.getTotal();
		if (percentOffline >= percentThreshold) {
			errorInfo = "Number of units for lab " + currentLab.getLabName()
					+ " reporting Offline is above threshold!";
			logger.error(errorInfo);
			currentLab.setBelowThreshold(true);
		}
		// check for zero data error
		if (numAvail == 0 && numInUse == 0 && numOffline == 0) {
			logger.trace("Detected zero data error, will continue with next scheduled run");
		}
	}

}