package htmlhandling;

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import setup.PropertyManager;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import dataobjects.Printer;


/**
 * Created by Jordi Diaz on 12/22/14. Designed to open browser, navigate to
 * status web page, find HTML code for current status of PC's then fetch that
 * specific HTML div and save it locally for parsing by separate class
 */
public class HTMLScraper {

	private Map<String, String> scraperProperties = new HashMap<String, String>();
	private Map<String, String> printerURLs = new HashMap<String, String>();
	private Boolean scrapeSuccess = false;
	private String scrapedHTML = null;
	
	private Printer currentPrinter = new Printer();

	private static final Logger logger = LogManager.getLogger("PrintStatusExtractor");

	public void run() throws IOException, SQLException, InterruptedException {
		PropertyManager propManager = PropertyManager.getPropertyManagerInstance();
		
		this.scraperProperties = propManager.getScraperProperties();		
		this.printerURLs = propManager.getPrinterURLs();	
		logger.trace("Properties Set, Starting Scraping Process!");
		iterateURLsAndScrape();
	}
	
	private void iterateURLsAndScrape() throws IOException, InterruptedException, SQLException {
		Iterator<Entry<String, String>> it = printerURLs.entrySet().iterator();
		while (it.hasNext()) {
			logger.trace("*-----HTMLScraper Is Starting!-----*");
			Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
			scrapeSuccess = false;
			logger.trace("Attempting To Scrape " + pair.getValue());
			getHtmlFromPage(pair.getValue());
			currentPrinter.setScrapedHTML(scrapedHTML);
			if (scrapeSuccess) {
				logger.trace("Scrape Successful, Commencing Parsing");
				HTMLParser parser = new HTMLParser();
				parser.run(currentPrinter);
			}
		}
	}

	// Takes URL for map page, loads map page into memory
	// searches for status div "the-pieces" and saves relevant html to lab object
	private void getHtmlFromPage(String url) throws IOException, InterruptedException {
		String htmlString;
		// To keep track of whether loads are successful
		boolean pageLoaded = true;
		boolean divLoaded = false;
		// Gets Page, will currently only load script output for Firefox 24
		// will throw warnings and alerts, should be sorted.
		WebClient printerStatusClient = new WebClient(BrowserVersion.CHROME);
		HtmlPage statusPage = null;
		// Initial attempt to load URL, updates pageLoaded Boolean
		try {
			statusPage = printerStatusClient.getPage(url);
		} catch (UnknownHostException a) {
			logger.error(a);
			// Update Boolean to direct rest of process
			pageLoaded = false;
			// Close current web client
			printerStatusClient.closeAllWindows();
		}
		// If page loaded successfully, continue with scrape attempt
		if (pageLoaded) {
			// Checks whether div has been found
			if (!divLoaded) {
				// Loop to keep track of number of attempts to retrieve HTML
					// Check page for requested div
					if (statusPage.getElementById("the-pieces") != null) {
					
				}
			}
			// If no data was found update Boolean and log error
			if (!divLoaded) {
				logger.error("could not load html!");
			}
		}
	}

}