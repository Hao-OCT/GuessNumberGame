package Multiplayer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public static void main(String[] args) {
		ServerSocket serverSocket;
		Socket socket = null;
		DataInputStream in;
		DataOutputStream out;
		int count=0;
		try {
			serverSocket = new ServerSocket(61099);	
			System.out.println("The Server is running...");
			while (true) {
				socket = serverSocket.accept();
				count++;
				System.out.println("The current number of clients is "+count);
				in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF("Please enter your first name to register");
				//new ServerThread(in.readUTF()).run();
				out.writeUTF("Please wait for the next round...");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void register() {

	}
}

class ServerThread extends Thread {
	private String text;

	public ServerThread(String text) {
		this.text = text;
		setDaemon(true);
	}

	public void run() {
		System.out.println("Thread "+text+" is working");
		this.setName(text);
		
	}
}