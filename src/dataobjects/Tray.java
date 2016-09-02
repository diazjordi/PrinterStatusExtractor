package dataobjects;

public class Tray{

	private String trayType;
	private String status;
	private int capacity;
	private String size;
	private String type;
	
	public Tray() {
		super();
	}
	public Tray(String trayType, String status, int capacity, String size, String type) {
		super();
		this.trayType = trayType;
		this.status = status;
		this.capacity = capacity;
		this.size = size;
		this.type = type;
	}
	
	public String getTrayType() {
		return trayType;
	}
	public void setTrayType(String trayType) {
		this.trayType = trayType;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "Tray [trayType=" + trayType + ", status=" + status
				+ ", capacity=" + capacity + ", size=" + size + ", type="
				+ type + "]";
	}	
}
