package Multiplayer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server {
	protected static List<ServerThread> clients;
	protected static boolean canStart = false;

	public static void main(String[] args) {
		ServerSocket serverSocket;
		Socket socket = null;
		DataInputStream in;
		DataOutputStream out;
		clients = new ArrayList<ServerThread>();

		try {
			serverSocket = new ServerSocket(61099);
			System.out.println("The Server is running...");
			while (true) {
				socket = serverSocket.accept();
				in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF("Please enter your first name to register");
				ServerThread thread = new ServerThread(socket, in.readUTF());
				clients.add(thread);

				System.out.println("The current number of clients is " + clients.size());
				out.writeUTF("Please wait for the next round...");

				thread.start();

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
	private long startTime;
	private long elapsedTime;

	public ServerThread(Socket socket, String text) {
		this.socket = socket;
		this.text = text;
		setDaemon(true);
		this.setName(text);
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public void run() {
		try {
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
			if (Server.clients.size() == 1) {
				Server.clients.get(0).setStartTime(System.currentTimeMillis());
				while (Server.clients.get(0).getElapsedTime() < 0.2 * 60 * 1000) {
					Server.clients.get(0).setElapsedTime((new Date()).getTime() - Server.clients.get(0).getStartTime());
				}
			} else if (Server.clients.size() > 1 && Server.clients.size() <= 3) {
				this.sleep((long) (0.2 * 60 * 1000 - Server.clients.get(0).getElapsedTime()));
			} else {
				this.sleep(10 * 60 * 1000);
			}
			String names = "";
			int max = Server.clients.size();
			if (max > 3)
				max = 3;
			for (int i = 0; i < max; i++) {
				names += Server.clients.get(i).getName() + " ";
			}
			out.writeUTF("Game starts and the player list: " + names);

			// Now the game start.

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}