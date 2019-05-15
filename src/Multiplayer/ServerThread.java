package Multiplayer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ServerThread extends Thread {
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private String name = "";
	private boolean done = false;
	private boolean playAgain = false;
	private PrintWriter pw1 = null;
	private PrintWriter pw2 = null;
	private Timestamp timeStamp = new Timestamp(System.currentTimeMillis());

	public ServerThread(Socket socket) {
		this.socket = socket;
		try {
			pw1 = new PrintWriter("Gamming.txt");
			pw2 = new PrintWriter("Communication.txt");
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
		}
	}

	public void registerName() {
		try {
			System.out.println("Connection from " + socket.getInetAddress());
			writeToFile(1,"Connection from " + socket.getInetAddress());
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("Please enter your first name to register");
			writeToFile(3, "[Server]Please enter your first name to register");
			name = in.readUTF();
			writeToFile(3, "[Client]"+name);

			Server.waitingList.add(name);

			out.writeUTF("Please be patient, the game is about to start...");
			writeToFile(3, "[Server]Please be patient, the game is about to start...");
			int total = Server.waitingList.size() + Server.playerList.size();
			System.out.println("The current number of clients is " + total);
			writeToFile(1, "Please be patient, the game is about to start...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeToFile(int i, String input) {
		if(i==1) {
    		pw1.println("["+timeStamp+"]");
        	pw1.println(input);
    	}
    	else if (i==2) {
    		pw2.println("["+timeStamp+"]");
        	pw2.println(input);
    	}
    	else {
    		pw1.println("["+timeStamp+"]");
        	pw1.println(input);
    		pw2.println("["+timeStamp+"]");
        	pw2.println(input);
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
			if (Server.playerList.contains(this.name)) {
				out.writeBoolean(true);
				out.writeUTF("Player " + this.name + ", you are in the coming round");
				break;

			} else {
				out.writeBoolean(false);
			}
		}

	}

	public void gameON() {
		try {
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());
			// Only let the first player to have access to invoke
			// the random number generator
			if (Server.playerList.size() < 3) {
				if (name.equals(Server.playerList.get(0))) {
					Server.randomNum = randomGenerator();
					countdown();
				} else {
					// clients can reach here are in the game
					Thread.sleep((long) (1 * 60 * 1000 - Server.elapsedTime));
				}
			}

			String names = "";
			for (int i = 0; i < Server.playerList.size(); i++) {
				names += Server.playerList.get(i) + " ";
			}
			out.writeUTF("Game starts and the player list: " + names);
			writeToFile(3, "[Server]Game starts and the player list:"+ names);
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
		while (Server.elapsedTime < 1 * 60 * 1000) {
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
			writeToFile(2, "[Client:"+this.name+"]"+guess);
			times--;
			if (guess.equals("e")) {
				out.writeBoolean(true);
				out.writeUTF("You exit this round");
				writeToFile(3, "[Server]You exit this round");
				Server.timesMap.put(this.name, 4);
				break;
			} else if (Integer.parseInt(guess) > 9 || Integer.parseInt(guess) < 0) {
				// validates the number guessed by the client
				out.writeBoolean(false);
				out.writeUTF("Your guess is beyond the range 0-9, remaining chance is " + times);
				writeToFile(3, "[Server]Your guess is beyond the range 0-9, remaining chance is " + times);
			} else if (Integer.parseInt(guess) == Server.randomNum) {
				out.writeBoolean(true);
				Server.timesMap.put(name, 4 - times);
				out.writeUTF("You made the right guess!");
				writeToFile(3, "[Server]You made the right guess!");
				break;
			} else if (Integer.parseInt(guess) > Server.randomNum) {
				if (times != 0) {
					out.writeBoolean(false);
					out.writeUTF(larger + times);
					writeToFile(3, "[Server]"+larger + times);
				} else {
					out.writeBoolean(true);
					out.writeUTF(answer + Server.randomNum);
					writeToFile(3, "[Server]"+answer + Server.randomNum);
					Server.timesMap.put(this.name, 4);
				}
			} else {
				if (times != 0) {
					out.writeBoolean(false);
					out.writeUTF(smaller + times);
					writeToFile(3, "[Server]"+smaller + times);
				} else {
					out.writeBoolean(true);
					out.writeUTF(answer + Server.randomNum);
					writeToFile(3, "[Server]"+answer + Server.randomNum);
					Server.timesMap.put(this.name, 4);
				}
			}
		}
		done = true;
		Server.doneMap.replace(this.name, false, this.done);
		// after one thread finish the guess, set the done to true
		// when all the thread done, then the Map contains all true value.
		while (Server.gameOver == false) {
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
			while (Server.timesMap.size() != Server.playerList.size()) {

			}
			if (Server.timesMap.size() == Server.playerList.size() && Server.gameOver) {
				out.writeUTF("Ranking: " + sortHashMapByValues(Server.timesMap).keySet());
				writeToFile(3, "[Server]"+"Ranking: " + sortHashMapByValues(Server.timesMap).keySet());
			}
			out.writeUTF("Play again (p) or Quit(q)");
			writeToFile(3, "[Server]"+"Play again (p) or Quit(q)");
			
			String reply = "";
			reply = in.readUTF();
			writeToFile(3, "[Client:"+this.name+"]"+reply);
			if (reply.equals("p")) {
				this.playAgain = true;
				out.writeBoolean(true);
				out.writeUTF("You choose play again, wait in the line");
				writeToFile(3, "[Server]You choose play again, wait in the line");
			} else if (reply.equals("q")) {
				this.playAgain = false;
				out.writeBoolean(false);
				out.writeUTF("You choose quit, wish you enjoy the game");
				writeToFile(3, "[Server]You choose quit, wish you enjoy the game");
			} else {
				this.playAgain = false;
				out.writeBoolean(false);
				out.writeUTF("How dare you type " + reply
						+ " rather than q or p! But anyway, you won't have another chance");
				writeToFile(3, "[Server]"+"How dare you type " + reply
						+ " rather than q or p! But anyway, you won't have another chance");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public LinkedHashMap<String, Integer> sortHashMapByValues(HashMap<String, Integer> passedMap) {
		List<String> mapKeys = new ArrayList<>(passedMap.keySet());
		List<Integer> mapValues = new ArrayList<>(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

		Iterator<Integer> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			int val = valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				String key = keyIt.next();
				int comp1 = passedMap.get(key);
				int comp2 = val;

				if (comp1 == comp2) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

	public void endRound() throws IOException {
		// remove the name in the nameList
		// reset the boolean value and wipe the data
		// Client involved in this round delete one client

		if (Server.playerList.contains(this.name)) {
			Server.gameStart = false;
			Server.gameOver = false;
			Server.playerList.clear();
			Server.timesMap.clear();
			Server.doneMap.clear();
			Server.startTime = 0;
			Server.elapsedTime = 0;

		}
		this.done = false;
		// if play again
		in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		out = new DataOutputStream(socket.getOutputStream());

		if (this.playAgain) {
			Server.waitingList.add(this.name);
			out.writeUTF("Register you in the back of the line..");
			writeToFile(3, "[Server]Register you in the back of the line..");
		} else {
			// if quit, then kill it
			this.interrupt();
		}
	}

	public void run() {

		try {
			synchronized (this) {

				registerName();
				while (!this.isInterrupted()) {
					checkQueue();
					gameON();
					guess();
					announceWinner();
					endRound();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				in.close();
				out.close();
				socket.close();
				pw1.close();
				pw2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}
}
