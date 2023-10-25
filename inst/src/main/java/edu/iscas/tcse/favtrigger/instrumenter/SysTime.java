package edu.iscas.tcse.favtrigger.instrumenter;

public class SysTime {
	public static native long rdtsc();
	public static native long rdtscp();
}
