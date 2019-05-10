package Multiplayer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	public static void main(String[] args) throws IOException {
		Socket socket = null;
		BufferedReader reader;
		DataOutputStream out = null;
		DataInputStream in = null;
		boolean success = false;
		boolean gameOver = false;
		boolean playAgain = true;
		boolean start = false;
		try {
			socket = new Socket("localhost", 61099);
			reader = new BufferedReader(new InputStreamReader(System.in));
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			System.out.println(in.readUTF());
			out.writeUTF(reader.readLine()); // output the name
			while (playAgain) {
				playAgain = false;
				System.out.println(in.readUTF());// Please be patient, the game is about to start
				System.out.println(in.readUTF()); // player (name), take a deep breath
				start = in.readBoolean();
				while (!start) {
					System.out.println(in.readUTF());
					System.out.println(start);
					start = in.readBoolean();
				}
				System.out.println(in.readUTF()); // announce the game starts

				while (!success) {
					// guess time
					out.writeUTF(reader.readLine());
					out.flush();
					success = in.readBoolean();
					System.out.println(in.readUTF());

				}
				while (!gameOver) {
					// wait for the announcement
					gameOver = in.readBoolean();
				}
				System.out.println(in.readUTF());// announce the winner
				System.out.println(in.readUTF());// play again or quit
				out.writeUTF(reader.readLine());// output q or p
				playAgain = in.readBoolean();
				System.out.println(in.readUTF());
			}

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			in.close();
			out.close();
			socket.close();
		}
	}
}
