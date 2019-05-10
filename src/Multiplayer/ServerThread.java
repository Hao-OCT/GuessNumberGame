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
	private boolean done = false;
	private boolean playAgain = false;

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

			Server.waitingList.add(name);

			out.writeUTF("Please be patient, the game is about to start...");
			System.out.println("The current number of clients is " + Server.waitingList.size());
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
		while (true) {
			if (Server.playerList.size() < 3 && Server.waitingList.size() > 0) {
				Server.playerList.add(Server.waitingList.get(0));
				Server.waitingList.remove(0);
			}
			System.out.println("after " + Server.playerList);
			System.out.println("after " + Server.waitingList);
			if (Server.playerList.contains(this.name)) {
				out.writeUTF("Player " + this.name + ", you are in the coming round");
				out.writeBoolean(true);
				break;

			} else {
				out.writeUTF("Player " + this.name + ", you are in the waiting list");
				out.writeBoolean(false);
				try {

					Thread.sleep(8000);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					this.interrupt();
				}
			}
		}
//		while (Server.gameStart == false) {
//			if (Server.playerList.get(0).equals(name)) {
//				out.writeUTF("Player " + name + ", you are in the coming round");
//
//				out.writeBoolean(true);
//				break;
//			}
//
//			else if (Server.playerList.get(1).equals(name)) {
//				out.writeUTF("Player " + name + ", you are in the coming round");
//
//				out.writeBoolean(true);
//				break;
//			} else if (Server.playerList.get(2).equals(name)) {
//				out.writeUTF("Player " + name + ", you are in the coming round");
//
//				out.writeBoolean(true);
//				break;
//			} else {
//				try {
//					out.writeUTF("Keep waiting in the queue..");
//
//					out.writeBoolean(false);
//					Thread.sleep(5000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		while (Server.gameStart == true) {
//			out.writeUTF("Sorry the game already started, please be patient..");
//
//			out.writeBoolean(false);
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//
	}

	public void gameON() {
		try {
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
			// Only let the first player to have access to invoke
			// the random number generator
			if (name.equals(Server.playerList.get(0))) {
				Server.randomNum = randomGenerator();
				countdown();
			} else {
				// clients can reach here are in the game
				Thread.sleep((long) (0.2 * 60 * 1000 - Server.elapsedTime));
			}
			String names = "";
			for (int i = 0; i < Server.playerList.size(); i++) {
				names += Server.playerList.get(i) + " ";
			}
			out.writeUTF("Game starts and the player list: " + names);
			Server.gameStart = true;
			Server.doneMap.put(this.name, this.done);
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
		String guess = "";
		while (times > 0) {
			guess = in.readUTF();
			times--;
			if (guess.equals("e")) {
				out.writeBoolean(true);
				out.writeUTF("You exit this round");
				break;
			} else if (Integer.parseInt(guess) == Server.randomNum) {
				out.writeBoolean(true);
				Server.timeMap.put(name, (int) System.currentTimeMillis());
				out.writeUTF("You made the right guess!");
				break;
			} else if (Integer.parseInt(guess) > Server.randomNum) {
				if (times != 0) {
					out.writeBoolean(false);
					out.writeUTF(larger + times);
				} else {
					out.writeBoolean(true);
					out.writeUTF(answer + Server.randomNum);
				}
			} else {
				if (times != 0) {
					out.writeBoolean(false);
					out.writeUTF(smaller + times);
				} else {
					out.writeBoolean(true);
					out.writeUTF(answer + Server.randomNum);
				}
			}
		}
		done = true;
		Server.doneMap.replace(this.name, false, this.done);
		// after one thread finish the guess, set the done to true
		// when all the thread done, then the Map contains all true value.
		while (!Server.gameOver) {
			if (Server.doneMap.containsValue(false)) {
				Server.gameOver = false;
				out.writeBoolean(false);
			} else {
				Server.gameOver = true;
				out.writeBoolean(true);
				break;
			}
		}
	}

	public void announceWinner() {
		try {
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
			String winner = "";
			if (Server.timeMap.isEmpty() && Server.gameOver) {
				out.writeUTF("No one wins...");
			} else {
				// According to who get the right number before everyone else
				// comparing the time when they made the right guess
				int min = (int) Server.elapsedTime;
				for (String key : Server.timeMap.keySet()) {
					if (Server.timeMap.get(key) < min) {
						min = Server.timeMap.get(key);
						winner = key;
					}
				}
				out.writeUTF("The winner of this round is " + winner);
			}
			out.writeUTF("Play again (p) or Quit(q)");
			String reply = "";
			reply = in.readUTF();
			if (reply.equals("p")) {
				this.playAgain = true;
				out.writeBoolean(true);
				out.writeUTF("You choose play again, wait in the line");
			} else if (reply.equals("q")) {
				this.playAgain = false;
				out.writeBoolean(false);
				out.writeUTF("You choose quit, wish you enjoy the game");
			} else {
				this.playAgain = false;
				out.writeBoolean(false);
				out.writeUTF("How dare you type " + reply
						+ " rather than q or p! But anyway, you won't have another chance");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void endRound() throws IOException {
		// remove the name in the nameList
		// reset the boolean value and wipe the data
		// Client involved in this round delete one client
		if (Server.playerList.contains(this.name)) {
			Server.gameStart = false;
			Server.gameOver = true;
			Server.playerList.clear();
			Server.timeMap.clear();
			Server.doneMap.clear();
		}
		// if play again
		in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		out = new DataOutputStream(socket.getOutputStream());
		if (this.playAgain) {
			Server.waitingList.add(this.name);
			out.writeUTF("Register you in the back of the line..");
		} else {
			// if quit, then kill it
			this.interrupt();
		}
	}

	public void run() {

		try {

			registerName();
			while (!this.isInterrupted()) {
				checkQueue();
				gameON();
				guess();
				announceWinner();
				endRound();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
