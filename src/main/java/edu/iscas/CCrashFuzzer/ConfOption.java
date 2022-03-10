package edu.iscas.CCrashFuzzer;

public enum ConfOption {
	PRETREATMENT, //the script used to prepare the initial enviroment for a test
	WORKLOAD, //the workload to be tested (including starting the system)
	MONITOR, //the script used to collect trace and logs generated at test runtime
	UPDATE_CRASH, //the script to update current fault sequence (CUR_CRASH_FILE) to every node in the cluster
	CRASH, //the script to crash a node according to the ip
	RESTART, //the script to restart a node according to the ip
	BUG_REPORT, // the path to put generated bug reports
	CUR_CRASH_FILE, //the file to store the current fault sequence
	MONITOR_DIRS, //the path to put traces and logs
	CHECKER, //the script to check failure symptoms
	FAULT_CSTR, //the fault constraints, e.g., "2:{ip1,ip2,ip3};1:{ip4,ip5}" means for nodes {ip1,ip2,ip3},
	            //the max down nodes at same time is 2; for nodes {ip4,ip5}, the max down nodes at same time is 1
	TEST_TIME,  //limit the max test time, e.g., "20m" means max test time is 20 minutes
	HANG_TMOUT, //define the time to decide a hang bug
	MAP_SIZE, //size of the map that are used to store coverage info. We now do not use it.
	AFL_PORT,  // the port used for crashfuzzer controller to contact with the system to save coverage map and io traces
	WINDOW_SIZE, //do not use for now
	MAX_FAULTS, //if it is configured, it defines the max number of the injected faults in a test.
	ROOT_DIR
}
