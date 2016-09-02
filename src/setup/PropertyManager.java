package setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class PropertyManager {

	private static PropertyManager propertyManagerInstance;

	private static String propertyFilePath = "../Properties/PrintStatusExtractorProps.properties";
	private static Properties mainProperties = new Properties();

	private static Map<String, String> scraperProperties = new HashMap<String, String>();
	private static Map<String, String> printerURLs = new HashMap<String, String>();
	private static Map<String, String> htmlProperties = new HashMap<String, String>();

	private static final Logger logger = LogManager.getLogger("LabTracker");

	private PropertyManager() {
	}

	public static PropertyManager getPropertyManagerInstance() {
		if (null == propertyManagerInstance) {
			propertyManagerInstance = new PropertyManager();
		}
		return propertyManagerInstance;
	}

	public void loadProps() throws IOException {
		// Load prop file into main property object
		File mainPropertyFile = new File(propertyFilePath);
		FileInputStream mainInputStream = new FileInputStream(mainPropertyFile);
		mainProperties.load(mainInputStream);
		mainInputStream.close();
		// Check Property has actual values
		// if so proceed to retrieve properties
		if (!mainProperties.isEmpty()) {
			this.setProps();
		} else if (mainProperties.isEmpty()) {
			logger.error("No Props Found!");
		}
	}

	private void setProps() throws IOException {
		Set<Object> keys = mainProperties.keySet();
		for (Object k : keys) {
			String key = (String) k;
			if (key.startsWith("scraper")) {
				scraperProperties.put(key, mainProperties.getProperty(key));
			} else if (key.startsWith("html")) {
				htmlProperties.put(key, mainProperties.getProperty(key));
			}
		}
		retrievePrinterURLs();
	}

	private void retrievePrinterURLs() {
		// Temp Properties object to load props from file
		Properties labURLProps = new Properties();
		// Test for LabURLs property
		if (!mainProperties.getProperty("scraperLabURLsFile").isEmpty()) {
			try {
				File labUrlFile = new File(mainProperties.getProperty("scraperPrinterURLsFile"));
				FileInputStream labFileInput = new FileInputStream(labUrlFile);
				labURLProps.load(labFileInput);
				Enumeration<?> labURLKeys = labURLProps.keys();
				while (labURLKeys.hasMoreElements()) {
					String labProp = labURLKeys.nextElement().toString();
					if (!labURLProps.getProperty(labProp).isEmpty()) {
						printerURLs.put(labProp, labURLProps.getProperty(labProp));
					} else if (labURLProps.getProperty(labProp).isEmpty()) {
						//
					}
				}
			} catch (IOException e) {
				logger.error(e);
			}
		} else if (mainProperties.getProperty("printerURLsFile").isEmpty()) {
			logger.error("No Printer URLs found!");
		}
	}

	public Map<String, String> getScraperProperties() {
		return scraperProperties;
	}

	public Map<String, String> getPrinterURLs() {
		return printerURLs;
	}

	public Map<String, String> getHtmlProperties() {
		return htmlProperties;
	}

}