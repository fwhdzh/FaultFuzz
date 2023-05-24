package edu.iscas.tcse.favtrigger.taint;

public enum FAVTaintType {
	//abandon NATIVERTN in the future?
	//combine it with LinkType in the future?
	NEW,
    NEWARRAY,
    ANEWARRAY,
    MULTIANEWARRAY,
    INT, //new int
    CONSTANT, //push constant
    NATIVERTN,
    TIME,
	FIS,//FileInputStream
    PDSI,//PlainDatagramSocketImpl
    SIS,//SocketInputstream
    SCI,//SocketChannelImpl
    REFLECTION, //new taints constructed at reflection invoke
    HDFSREAD,
    RPC,
    ZKMSG,
    LVDBREAD
}