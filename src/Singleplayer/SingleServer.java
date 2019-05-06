package Singleplayer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class SingleServer {
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		Socket socket = null;
		Random random;
		int number;
		DataInputStream in = null;
		DataOutputStream out = null;
		int times = 4;
		try {
			serverSocket = new ServerSocket(61099);
			while (true) {
				socket = serverSocket.accept();
				random = new Random();
				number = random.nextInt(9);
				System.out.println(number);
				in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				out = new DataOutputStream(socket.getOutputStream());
				String larger = "Your guess is larger than the number, remaining chance is ";
				String smaller = "Your guess is smaller than the number, remaining chance is ";
				String answer = "You are running out of the chances, and the answer is ";
				int guess = -1;
				while (times > 0) {
					guess = in.readInt();
					times--;
					if (guess == number) {
						out.writeBoolean(true);
						out.writeUTF("Congratulations!");
						break;
					} else if (guess > number) {
						if (times != 0) {
							out.writeBoolean(false);
							out.writeUTF(larger + times);
						} else {
							out.writeBoolean(false);
							out.writeUTF(answer + number);
						}
					} else {
						if (times != 0) {
							out.writeBoolean(false);
							out.writeUTF(smaller + times);
						} else {
							out.writeBoolean(false);
							out.writeUTF(answer + number);
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			in.close();
			out.close();
			socket.close();
			serverSocket.close();
		}
	}
}
