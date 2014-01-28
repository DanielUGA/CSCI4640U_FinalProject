package ca.uoit.Allen.Matt.DSFinal;

import processing.core.*;

import java.io.*;

public class MineRobotClient {
	
	public static String hostname = "localhost";
	public static int serverPort = 3333;
	static BufferedReader stdIn;
	
	public static void main(String[] args) {
		
		stdIn = new BufferedReader(new InputStreamReader(System.in));
		if (args.length == 1)
			serverPort = Integer.parseInt(args[0]);
		if (args.length == 2) {
			hostname = args[0];
			serverPort = Integer.parseInt(args[1]);
		}
		
		PApplet.main("ca.uoit.Allen.Matt.DSFinal.MineRobotClientApplet");
	}
}
