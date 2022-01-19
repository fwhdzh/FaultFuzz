package edu.iscas.CCrashFuzzer;

public class CannotInjectAllFaultsException extends RuntimeException  {
	private String retCode ;
	private String msgDes;
	
	public CannotInjectAllFaultsException() {
		super();
	}
 
	public CannotInjectAllFaultsException(String message) {
		super(message);
		msgDes = message;
	}
 
	public CannotInjectAllFaultsException(String retCd, String msgDes) {
		super();
		this.retCode = retCd;
		this.msgDes = msgDes;
	}
 
	public String getRetCd() {
		return retCode;
	}
 
	public String getMsgDes() {
		return msgDes;
	}
}
