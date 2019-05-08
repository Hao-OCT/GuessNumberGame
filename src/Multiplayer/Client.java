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
		boolean start = false;
		boolean success = false;
		int times = 4;
		try {
			socket = new Socket("localhost", 61099);
			reader = new BufferedReader(new InputStreamReader(System.in));
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			System.out.println(in.readUTF());
			out.writeUTF(reader.readLine());
			System.out.println(in.readUTF());
			System.out.println(in.readUTF());
			System.out.println(in.readUTF());
			while (!success) {
				out.writeInt(Integer.parseInt(reader.readLine()));
				out.flush();
				success = in.readBoolean();
				System.out.println(in.readUTF());
				times--;
				if (times == 0)
					break;
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
