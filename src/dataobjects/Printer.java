package dataobjects;

import java.util.ArrayList;

public class Printer {
	
	private String printerModel;
	private String ipAddress;
	private String printerType;
	private int cartridgeVolume;
	private ArrayList<Tray> trays = new ArrayList<Tray>();
	private String scrapedHTML;

	public Printer() {
		super();
	}

	public Printer(String labName) {
		this.printerType = labName;
	}

	public String getPrinterModel() {
		return printerModel;
	}

	public void setPrinterModel(String printerModel) {
		this.printerModel = printerModel;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getPrinterType() {
		return printerType;
	}

	public void setPrinterType(String printerType) {
		this.printerType = printerType;
	}

	public int getCartridgeVolume() {
		return cartridgeVolume;
	}

	public void setCartridgeVolume(int cartridgeVolume) {
		this.cartridgeVolume = cartridgeVolume;
	}
	
	public void addTray(Tray tray){
		this.trays.add(tray);
	}
	public ArrayList<Tray> getStations() {
		return trays;
	}

	public void setStations(ArrayList<Tray> stations) {
		this.trays = stations;
	}

	public String getScrapedHTML() {
		return scrapedHTML;
	}

	public void setScrapedHTML(String scrapedHTML) {
		this.scrapedHTML = scrapedHTML;
	}

	@Override
	public String toString() {
		return "Printer [printerModel=" + printerModel + ", ipAddress="
				+ ipAddress + ", printerType=" + printerType
				+ ", cartridgeVolume=" + cartridgeVolume + ", stations="
				+ trays + ", scrapedHTML=" + scrapedHTML + "]";
	}
}
