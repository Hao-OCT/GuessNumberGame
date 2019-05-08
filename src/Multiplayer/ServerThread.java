package Multiplayer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Random;

public class ServerThread extends Thread {
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private String name = "";
	private long timestamp;

	public ServerThread(Socket socket) {
		this.socket = socket;
	}

	public void registerName() {
		try {
			System.out.println("Connection from " + socket.getInetAddress());
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("Please enter your first name to register");
			name = in.readUTF();
			Server.nameList.add(name);
			out.writeUTF("Please be patient, the game is about to start...");
			System.out.println("The current number of clients is " + Server.nameList.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void checkQueue() throws IOException {
		/*
		 * if the round doesn't start, then pick the first three clients (if the clients
		 * // >=3) // or pick all of the clients (if the clients<3) // check if the name
		 * of this client name matches the nameList order (first 3) // if the round
		 * start, keep waiting
		 */
		if (Server.nameList.size() > 3) {
			Server.playerNum = 3;
		} else {
			Server.playerNum = Server.nameList.size();
		}
		while (Server.gameStart == false) {
			if (Server.nameList.get(0).equals(name)) {
				out.writeUTF("Player " + name + ", take a deep breath and let's go");
				break;
			}

			else if (Server.nameList.get(1).equals(name)) {
				out.writeUTF("Player " + name + ", take a deep breath and let's go");
				break;
			} else if (Server.nameList.get(2).equals(name)) {
				out.writeUTF("Player " + name + ", take a deep breath and let's go");
				break;
			} else {
				try {
					out.writeUTF("Keep waiting in the queue..");
					this.sleep(10 * 60 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//
		while (Server.gameStart == true) {
			out.writeUTF("Sorry the game already started, please be patient..");
			try {
				this.sleep(10 * 60 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void gameON() {
		try {
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
			// Only let the first player to have access to invoke
			// the random number generator
			if (name.equals(Server.nameList.get(0))) {
				Server.randomNum = randomGenerator();
				countdown();
			} else {
				// clients can reach here are in the game
				this.sleep((long) (0.2 * 60 * 1000 - Server.elapsedTime));
			}
			String names = "";
			for (int i = 0; i < Server.playerNum; i++) {
				names += Server.nameList.get(i) + " ";
			}
			out.writeUTF("Game starts and the player list: " + names);
			Server.gameStart = true;
			// now the game starts..
		} catch (IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public int randomGenerator() {
		Random random = new Random();
		return random.nextInt(9);
	}

	public void countdown() {
		Server.startTime = System.currentTimeMillis();
		while (Server.elapsedTime < 0.2 * 60 * 1000) {
			Server.elapsedTime = ((new Date()).getTime() - Server.startTime);
		}
	}

	public void guess() throws IOException {
		int times = 4;
		in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		out = new DataOutputStream(socket.getOutputStream());
		// the same as the single version
		String larger = "Your guess is larger than the number, remaining chance is ";
		String smaller = "Your guess is smaller than the number, remaining chance is ";
		String answer = "You are running out of the chances, and the answer is ";
		int guess = -1;
		while (times > 0) {
			guess = in.readInt();
			times--;
			if (guess == Server.randomNum) {
				out.writeBoolean(true);
				Server.timeMap.put(name, (int) System.currentTimeMillis());
				out.writeUTF("Congratulations!");
				break;
			} else if (guess > Server.randomNum) {
				if (times != 0) {
					out.writeBoolean(false);
					out.writeUTF(larger + times);
				} else {
					out.writeBoolean(false);
					out.writeUTF(answer + Server.randomNum);
				}
			} else {
				if (times != 0) {
					out.writeBoolean(false);
					out.writeUTF(smaller + times);
				} else {
					out.writeBoolean(false);
					out.writeUTF(answer + Server.randomNum);
				}
			}
		}
	}

	public void announceWinner() {
		try {
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
			String winner="";
			if (Server.timeMap.isEmpty()) {
				out.writeUTF("No one wins...");
			} else {
				int min = (int) Server.elapsedTime;
				for (String key : Server.timeMap.keySet()) {
					if (Server.timeMap.get(key) < min) {
						min = Server.timeMap.get(key);
						winner = key;
					}
				}//TODO
				//after all the player finish, show the result.
				out.writeUTF("The winner of this round is " + winner);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {

		try {

			registerName();
			checkQueue();
			gameON();
			guess();
			announceWinner();
			// Now the game start.

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
