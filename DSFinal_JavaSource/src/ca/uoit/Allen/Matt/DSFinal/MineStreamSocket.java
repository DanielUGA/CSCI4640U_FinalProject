package ca.uoit.Allen.Matt.DSFinal;

import java.net.*;
import java.io.*;

public class MineStreamSocket extends Socket {
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	
	MineStreamSocket(InetAddress acceptorHost, int acceptorPort) throws SocketException, IOException {
		this.socket = new Socket(acceptorHost, acceptorPort);
		setStreams();
	}
	
	MineStreamSocket(Socket socket) throws IOException {
		this.socket = socket;
		setStreams();
	}
	
	private void setStreams() throws IOException {
		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	}
	
	protected void sendMessage(String message) throws IOException {
		output.println(message);
		output.flush();
	}

	protected String receiveMessage() throws IOException {
		String message = input.readLine();
		return message;
	}
}
