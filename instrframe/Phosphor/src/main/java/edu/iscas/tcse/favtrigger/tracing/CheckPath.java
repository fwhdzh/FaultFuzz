package edu.iscas.tcse.favtrigger.tracing;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class CheckPath {
	public enum LinkType {
		FILE, DATAGRAMSOCKET, SERVERSOCKET, SOCKET, INNER, OTHER
	}
    public static LinkType checkFileDescriptorSource(Closeable parent) {
    	if(parent == null) {
    		return LinkType.OTHER;
    	}
    	if(parent instanceof FileInputStream
    			|| parent instanceof FileOutputStream
    			|| parent instanceof RandomAccessFile) {
    		return LinkType.FILE;
    	} else if (parent instanceof DatagramSocket) {
    		return LinkType.DATAGRAMSOCKET;
    	} else if (parent instanceof ServerSocket) {
    		return LinkType.SERVERSOCKET;
    	} else if (parent instanceof Socket) {
    		return LinkType.SOCKET;
    	} else {
    		return LinkType.OTHER;
    	}
    }
}
