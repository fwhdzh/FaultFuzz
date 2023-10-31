package edu.iscas.tcse.backend;

import java.util.List;

class InstConf {
	String instPath;
	boolean useFaultFuzz;
	List<String> preDefinedInst;
	String observerHome;
	String dataPaths;
	String cacheDir;
	String controllerSocket;
	int mapSize;
	int wordSize;
	String covPath;
	String covIncludes;
	String aflAllow;
	String aflDeny;
	int aflPort;
	String annotationFile;
	public String getInstPath() {
		return instPath;
	}
	public void setInstPath(String instPath) {
		this.instPath = instPath;
	}
	public boolean isUseFaultFuzz() {
		return useFaultFuzz;
	}
	public void setUseFaultFuzz(boolean useFaultFuzz) {
		this.useFaultFuzz = useFaultFuzz;
	}
	public List<String> getPreDefinedInst() {
		return preDefinedInst;
	}
	public void setPreDefinedInst(List<String> preDefinedInst) {
		this.preDefinedInst = preDefinedInst;
	}
	public String getObserverHome() {
		return observerHome;
	}
	public void setObserverHome(String observerHomePath) {
		this.observerHome = observerHomePath;
	}
	public String getDataPaths() {
		return dataPaths;
	}
	public void setDataPaths(String dataPaths) {
		this.dataPaths = dataPaths;
	}
	public String getCacheDir() {
		return cacheDir;
	}
	public void setCacheDir(String cacheDir) {
		this.cacheDir = cacheDir;
	}
	public String getControllerSocket() {
		return controllerSocket;
	}
	public void setControllerSocket(String controllerSocket) {
		this.controllerSocket = controllerSocket;
	}
	public int getMapSize() {
		return mapSize;
	}
	public void setMapSize(int mapSize) {
		this.mapSize = mapSize;
	}
	public int getWordSize() {
		return wordSize;
	}
	public void setWordSize(int wordSize) {
		this.wordSize = wordSize;
	}
	public String getCovPath() {
		return covPath;
	}
	public void setCovPath(String covPath) {
		this.covPath = covPath;
	}
	public String getCovIncludes() {
		return covIncludes;
	}
	public void setCovIncludes(String covIncludes) {
		this.covIncludes = covIncludes;
	}
	public String getAflAllow() {
		return aflAllow;
	}
	public void setAflAllow(String aflAllow) {
		this.aflAllow = aflAllow;
	}
	public String getAflDeny() {
		return aflDeny;
	}
	public void setAflDeny(String aflDeny) {
		this.aflDeny = aflDeny;
	}
	public int getAflPort() {
		return aflPort;
	}
	public void setAflPort(int aflPort) {
		this.aflPort = aflPort;
	}
	public String getAnnotationFile() {
		return annotationFile;
	}
	public void setAnnotationFile(String annotationFile) {
		this.annotationFile = annotationFile;
	}
}