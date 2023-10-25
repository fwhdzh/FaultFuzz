package edu.iscas.tcse.faultfuzz.ctrl.model;

public enum FaultType {
	NO, //we may not use this stat
	CRASH, 
	REBOOT,
	NETWORK_DISCONNECTION,
	NETWORK_RECONNECTION
}