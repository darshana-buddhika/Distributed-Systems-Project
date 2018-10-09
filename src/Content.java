import java.util.ArrayList;

public class Content {
	
	private String fileName;
	private ArrayList<String> ipList;
	
	public Content(String fileName) {
		
		this.fileName = fileName;
		this.ipList = new ArrayList<>();
		
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public ArrayList<String> getIpList() {
		return ipList;
	}
	public void setIpList(String ipAddress) {
		this.ipList.add(ipAddress);
	}

}
