package br.com.cmabreu.zodiac.federation.objects;

import br.com.cmabreu.zodiac.scorpio.SystemProperties;
import hla.rti1516e.ObjectInstanceHandle;

public class ScorpioObject {
	private long freeMemory;		
	private long totalMemory;
	private double cpuLoad;
	private int availableProcessors;
	private String machineName;
	private String soName;
	private String macAddress;
	private String ipAddress;
	private ObjectInstanceHandle instance;
	private int totalInstances = 0;
	
	public int getTotalInstances() {
		return totalInstances;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public boolean isMe( ObjectInstanceHandle objHandle ) {
		if ( instance.equals( objHandle ) ) {
			return true;
		} else return false;
	}
	
	public ObjectInstanceHandle getHandle() {
		return instance;
	}	
	
	public void updateValues() {
		try {
			SystemProperties sp = SystemProperties.getInstance();
			freeMemory = sp.getFreeMemory();		
			totalMemory = sp.getTotalMemory();
			cpuLoad = sp.getCpuLoad();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public ScorpioObject( ObjectInstanceHandle instance ) throws Exception {
		SystemProperties sp = SystemProperties.getInstance();
		freeMemory = sp.getFreeMemory();		
		totalMemory = sp.getTotalMemory();
		cpuLoad = sp.getCpuLoad();
		availableProcessors = sp.getAvailableProcessors();
		availableProcessors = availableProcessors + ( availableProcessors / 2 );
		machineName = sp.getMachineName();
		soName = sp.getSoName();
		macAddress = sp.getMacAddress();
		ipAddress = sp.getLocalIpAddress();
		this.instance = instance;
	}

	public long getFreeMemory() {
		return freeMemory;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public double getCpuLoad() {
		return cpuLoad;
	}

	public void setTotalMemory(int totalMemory) {
		this.totalMemory = totalMemory;
	}

	public void setCpuLoad(float cpuLoad) {
		this.cpuLoad = cpuLoad;
	}

	public int getAvailableProcessors() {
		return availableProcessors;
	}

	public void setAvailableProcessors(int availableProcessors) {
		this.availableProcessors = availableProcessors;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getSoName() {
		return soName;
	}

	public void setSoName(String soName) {
		this.soName = soName;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	
	public void increaseTotalInstances() {
		totalInstances++;
	}	

}
