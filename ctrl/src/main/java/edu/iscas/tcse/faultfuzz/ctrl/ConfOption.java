package edu.iscas.tcse.faultfuzz.ctrl;

public enum ConfOption {
	
	WORKLOAD, //the workload to be tested (including starting the system)
	CHECKER, //the script to check failure symptoms

	FAULT_TYPE, // the fault type to be tested, e.g., "[CRASH,NETWORK_DISCONNECTION]"
	CRASH, //the script to crash a node according to the ip
	REBOOT, //the script to reboot a node according to the ip
	NETWORK_DISCONNECTION, //the script to disconnect network from a node to another node according to the ip
	NETWORK_RECONNECTION, //the script to reconnect network from a node to another node according to the ip
	MAX_FAULTS, //if it is configured, it defines the max number of the injected faults in a test.

	FAULT_CSTR, //the fault constraints, e.g., "2:{ip1,ip2,ip3};1:{ip4,ip5}" means for nodes {ip1,ip2,ip3},
	            //the max down nodes at same time is 2; for nodes {ip4,ip5}, the max down nodes at same time is 1
	AFL_PORT,  // the port used for faultfuzz controller to contact with the system to save coverage map and io traces
	MONITOR, //the script used to collect trace and logs generated at test runtime
	PRETREATMENT, //the script used to prepare the initial enviroment for a test

	ROOT_DIR,
	CUR_FAULT_FILE, //the file to store the current fault sequence
	CONTROLLER_PORT,

	TEST_TIME,  //limit the max test time, e.g., "20m" means max test time is 20 minutes
	HANG_TIMEOUT, //define the time to decide a hang bug
	DETERMINE_WAIT_TIME, //define the time to wait the cluster to trigger next IO point.
	
	BUG_REPORT, // the path to put generated bug reports
	MONITOR_DIRS, //the path to put traces and logs
	
	EVALUATE_TARGET,

	MAP_SIZE, //size of the map that are used to store coverage info. We now do not use it.
	WINDOW_SIZE, //do not use for now
	UPDATE_FAULT, //the script to update current fault sequence (CUR_FAULT_FILE) to every node in the cluster

	REPLAY_MODE,
	REPLAY_TRACE_PATH,
	RECOVERY_MODE,
	RECOVERY_DIR,
	// REPLAY_MODE,

	// WRITE_FAV_ENV,
	// COPY_ENV_TO_CLUSTER,
	// COPY_LOGS_TO_CONTROLLER,
	// CLUSTER_LOGS_IN_CONTROLLER_DIR,
	LOG_LEVEL
}
