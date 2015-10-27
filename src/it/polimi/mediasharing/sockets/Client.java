package it.polimi.mediasharing.sockets;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Client {
	
	public void sendMessage(String host, int port, int reason, String message) throws IOException {		
        sendToTheServer(host, port, reason, message);
    }
	
    public void sendFile(String host, int port, int reason, InputStream file) throws IOException {
    	BufferedInputStream bis = new BufferedInputStream(file);
        sendToTheServer(host, port, reason, bis);
    }
    
    public void sendToTheServer(String host, int port, int reason, String message) throws IOException {
    	Socket socket = new Socket(host, port);
    	DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        dos.writeInt(reason);
        dos.writeUTF(message);
        dos.close();        
        socket.close();
    }
    
    public void sendToTheServer(String host, int port, int reason, BufferedInputStream bis) throws IOException {
    	Socket socket = new Socket(host, port);
    	DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        byte[] buffer = new byte[8192];
        int count;
        dos.writeInt(reason);
        if(bis != null){
	        while ((count = bis.read(buffer)) > 0) {
	        	dos.write(buffer, 0, count);
	        }
        	bis.close();
    	}
        dos.close();        
        socket.close();
    }
}