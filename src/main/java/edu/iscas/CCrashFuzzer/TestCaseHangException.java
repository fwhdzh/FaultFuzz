package edu.iscas.CCrashFuzzer;

public class TestCaseHangException extends RuntimeException {
	private String retCode ;
	private String msgDes;
	
	public TestCaseHangException() {
		super();
	}
 
	public TestCaseHangException(String message) {
		super(message);
		msgDes = message;
	}
 
	public TestCaseHangException(String retCd, String msgDes) {
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
