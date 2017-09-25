package db;

public class Report {
	private String NoLogDecoder;
	private String CollectorName;
	private String DeviceType;
	private String DeviceIP;
	private String LogCoder;
	private String LastSeenTime;
	private String Count;
	private String normal;
	
	public String getNoLogDecoder() {
		return NoLogDecoder;
	}
	public void setNoLogDecoder(String noLogDecoder) {
		NoLogDecoder = noLogDecoder;
	}
	public String getCollectorName() {
		return CollectorName;
	}
	public void setCollectorName(String collectorName) {
		CollectorName = collectorName;
	}
	public String getDeviceType() {
		return DeviceType;
	}
	public void setDeviceType(String deviceType) {
		DeviceType = deviceType;
	}
	public String getDeviceIP() {
		return DeviceIP;
	}
	public void setDeviceIP(String deviceIP) {
		DeviceIP = deviceIP;
	}
	public String getLogCoder() {
		return LogCoder;
	}
	public void setLogCoder(String logCoder) {
		LogCoder = logCoder;
	}
	public String getLastSeenTime() {
		return LastSeenTime;
	}
	public void setLastSeenTime(String lastSeenTime) {
		LastSeenTime = lastSeenTime;
	}
	public String getCount() {
		return Count;
	}
	public void setCount(String count) {
		Count = count;
	}
	public String getNormal() {
		return normal;
	}
	public void setNormal(String normal) {
		this.normal = normal;
	}
	
}
