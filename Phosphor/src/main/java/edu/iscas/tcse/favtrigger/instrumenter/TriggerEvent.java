package edu.iscas.tcse.favtrigger.instrumenter;

public enum TriggerEvent {
	CRASH,  //crash current node
	RMCRS,  //crash remote node before reading the message
	CONTI, //keep execution
	FLIPCRASH,
	FLIPRMCRS,
	DELAYCRASH,
	DELAYRMCRS,
	REBOOT,

	//the following two events are not considered yet
	DROPM,  //drop current message sending event
	RMDRO,   //drop remote message receiving event
}
