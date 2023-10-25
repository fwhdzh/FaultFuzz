package edu.iscas.tcse.backend;

import java.util.List;

public class CtrlConf {

	String workload;
	String checker;
	List<String> faultType;
	String crash;
	String reboot;
	String networkDisconnection;
	String networkReconnection;
	String rootDir;
	String currentFaultFile;
    int controllerPort;
    String monitor;
	String preTreatment;
	String testTime;
	String faultCluster;
	int aflPort;
	String hangTimeOut;
	int maxFaults;
	int determineWaitTime;

	public String getWorkload() {
		return workload;
	}
	public void setWorkload(String workload) {
		this.workload = workload;
	}
	public String getChecker() {
		return checker;
	}
	public void setChecker(String checker) {
		this.checker = checker;
	}
	public List<String> getFaultType() {
		return faultType;
	}
	public void setFaultType(List<String> faultType) {
		this.faultType = faultType;
	}
	public String getCrash() {
		return crash;
	}
	public void setCrash(String crash) {
		this.crash = crash;
	}
	public String getReboot() {
		return reboot;
	}
	public void setReboot(String reboot) {
		this.reboot = reboot;
	}
	public String getNetworkDisconnection() {
		return networkDisconnection;
	}
	public void setNetworkDisconnection(String networkDisconnection) {
		this.networkDisconnection = networkDisconnection;
	}
	public String getNetworkReconnection() {
		return networkReconnection;
	}
	public void setNetworkReconnection(String networkReconnection) {
		this.networkReconnection = networkReconnection;
	}
	public String getRootDir() {
		return rootDir;
	}
	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}
	public String getCurrentFaultFile() {
		return currentFaultFile;
	}
	public void setCurrentFaultFile(String currentFaultFile) {
		this.currentFaultFile = currentFaultFile;
	}
    public int getControllerPort() {
        return controllerPort;
    }
    public void setControllerPort(int controllerPort) {
        this.controllerPort = controllerPort;
    }
	public String getMonitor() {
		return monitor;
	}
	public void setMonitor(String monitor) {
		this.monitor = monitor;
	}
	public String getPreTreatment() {
		return preTreatment;
	}
	public void setPreTreatment(String preTreatment) {
		this.preTreatment = preTreatment;
	}
	public String getTestTime() {
		return testTime;
	}
	public void setTestTime(String testTime) {
		this.testTime = testTime;
	}
	public String getFaultCluster() {
		return faultCluster;
	}
	public void setFaultCluster(String faultCluster) {
		this.faultCluster = faultCluster;
	}
	public int getAflPort() {
		return aflPort;
	}
	public void setAflPort(int aflPort) {
		this.aflPort = aflPort;
	}
	public String getHangTimeOut() {
		return hangTimeOut;
	}
	public void setHangTimeOut(String hangTimeOut) {
		this.hangTimeOut = hangTimeOut;
	}
	public int getMaxFaults() {
		return maxFaults;
	}
	public void setMaxFaults(int maxFaults) {
		this.maxFaults = maxFaults;
	}
	public int getDetermineWaitTime() {
		return determineWaitTime;
	}
	public void setDetermineWaitTime(int determineWaitTime) {
		this.determineWaitTime = determineWaitTime;
	}
	
	

	
}