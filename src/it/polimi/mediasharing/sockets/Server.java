package it.polimi.mediasharing.sockets;

import it.polimi.mediasharing.activities.MainActivity;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Handler;

public class Server extends Thread{
	
	Handler handler;
	int port;
	
	public Server(int port, Handler handler) throws IOException {
		this.handler = handler;
		this.port = port;
	}
	
	@Override
	public void run() {
		super.run();
		try {
			this.createFileServer(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    public void createFileServer(int port) throws IOException {         	    	
    	ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            System.out.println("Can't setup server on this port number. ");
        }

        Socket socket = null;
        BufferedInputStream bis = null;
        DataInputStream dis = null;

        int count = 0;
        while(count >= -1){
	        try {
	            socket = serverSocket.accept();
	        } catch (IOException ex) {
	            System.out.println("Can't accept client connection. ");
	        }
	        try {
	            bis = new BufferedInputStream(socket.getInputStream());
	            dis = new DataInputStream(bis);
	        } catch (IOException ex) {
	            System.out.println("Can't get socket input stream. ");
	        }
	        
	        int reason = dis.readInt();
	        switch (reason) {
			case MainActivity.SID:
				handler.sendMessage(handler.obtainMessage(MainActivity.SID, dis.readUTF()));
				break;
			default:
				break;
	        }
        }
        bis.close();
        socket.close();
        serverSocket.close();
    }
}