package Multiplayer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*  Assignment for Network Programming S1,2019
 *  @author: Hao Wang (s3690173)
 *  A.2 Multiplayer version
 *  The server can handle multiple clients at the 
 *  same time, and similar to the single version.
 *  But the server can manage and monitor the game.
 *  After each round, the server will announce the
 *  winner of this round and ask the client if they
 *  want to play again or quit the game.
 *  
 *  This is the Server end.
 */
public class Server {
	protected static List<String> playerList = new ArrayList<String>();
	protected static List<String> waitingList = new ArrayList<String>();
	protected static boolean gameStart=false;
	protected static boolean gameOver=false;
	protected static int randomNum=-1;
	protected static long startTime;
	protected static long elapsedTime;
	protected static HashMap<String,Integer> timesMap = new HashMap<String,Integer>();
	protected static Map<String,Boolean> doneMap = new HashMap<String,Boolean>();


	public static void main(String[] args) {
		final int port = 61099;
		ServerSocket serverSocket = null;
		Socket socket = null;

		try {
			serverSocket = new ServerSocket(port);
			System.out.println("The Server is running...");
			while (true) {
				socket = serverSocket.accept();
				new ServerThread(socket).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (socket != null)
					socket.close();
				if (serverSocket != null)
					serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
