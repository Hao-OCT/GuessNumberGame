package Multiplayer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Server {
	protected static int count = 0;
	protected static List<Thread> clients;

	public static void main(String[] args) {
		ServerSocket serverSocket;
		Socket socket = null;
		DataInputStream in;
		DataOutputStream out;
		clients=new ArrayList<Thread>();
		try {
			serverSocket = new ServerSocket(61099);
			System.out.println("The Server is running...");
			while (true) {
				socket = serverSocket.accept();
				in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF("Please enter your first name to register");
				new ServerThread(socket, in.readUTF()).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class ServerThread extends Thread {
	private Socket socket;
	private String text;

	private DataInputStream in;
	private DataOutputStream out;

	public ServerThread(Socket socket, String text) {
		this.socket = socket;
		this.text = text;
		setDaemon(true);
	}

	public void run() {
		try {
			Server.count++;
			Server.clients.add(this);
			System.out.println("The current number of clients is " + Server.count);
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
			
			out.writeUTF("Please wait for the next round...");
			System.out.println("Thread " + text + " is working");
			this.setName(text);
			while (Server.count < 3) {
				out.writeBoolean(false);
			}
			out.writeBoolean(true);
			String names = "";
			for(int i=0;i<Server.clients.size();i++) {
				names+=Server.clients.get(i).getName()+" ";
			}
			out.writeUTF("Game starts and the player lists: "+names);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}