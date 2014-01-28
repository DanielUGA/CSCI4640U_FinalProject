package ca.uoit.Allen.Matt.DSFinal;

import processing.core.*;
import java.net.*;
import java.util.*;

public class MineRobotClientApplet extends PApplet {

	private static final long serialVersionUID = 1L;
	public static final int windowWidth = 900;
	private static final int windowHeight = 500;
	private static final String hostname = MineRobotClient.hostname;
	private static final int serverPort = MineRobotClient.serverPort;
	private static final int CLIENT_AUTH = 2;
	public static int robotX = -10;
	private static int robotY = 0;
	MineStreamSocket mineDataSocket;
	private int gridHeight = 500;
	private int gridWidth = 900;
	private int mainGrid = 50;
	public static int minGrid = 10;
	public static ArrayList<Mine> mines;
	
	public void setup() {
		System.out.println("Attempting connection to: " + hostname + " on port: " + serverPort);
		mines = new ArrayList<Mine>();
		try {
			mineDataSocket = new MineStreamSocket(InetAddress.getByName(hostname), serverPort);
			mineDataSocket.sendMessage(String.valueOf(CLIENT_AUTH));
			// tell server about windowWidth
			mineDataSocket.sendMessage("distance,"+windowWidth);
			mineDataSocket.sendMessage("start,true");
			
			Thread readerThread = new Thread(new ReaderThread(mineDataSocket));
			readerThread.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		size(windowWidth, windowHeight);
		frame.setTitle("Mine Robot Client View");
		background(1);
	}
	
	public void draw() {
		fill(255, 255, 255, 10);
		rect(0, 0, windowWidth, windowHeight);
		fill(0, 0, 255);
		rect(robotX, robotY, 10, windowHeight);
		fill(255,0,0, 255);
		Mine[] minesArray = mines.toArray(new Mine[mines.size()]);
		for(Mine m : minesArray) {
			rect(m.x, m.y, m.width, m.height);
		}
		fill(0,0,0,255);
		drawGrid();
	}
	
	public void drawGrid() {
		for(int i = 0; i <= gridHeight; i += mainGrid) {
			stroke(0, 0, 0, 100);
			strokeWeight(2);
			line(0, i, gridWidth, i);
		}
		for(int i = 0; i <= gridHeight; i += minGrid) {
			stroke(0, 0, 0, 100);
			strokeWeight(1);
			line(0, i, gridWidth, i);
		}
		for(int i = 0; i <= gridWidth; i += mainGrid) {
			stroke(0, 0, 0, 100);
			strokeWeight(2);
			line(i, 0, i, gridHeight);
		}
		for(int i = 0; i <= gridWidth; i += minGrid) {
			stroke(0, 0, 0, 100);
			strokeWeight(1);
			line(i, 0, i, gridHeight);
		}
	}
}

class ReaderThread implements Runnable {

	MineStreamSocket mineDataSocket;
	
	ReaderThread(MineStreamSocket mineDataSocket) {
		this.mineDataSocket = mineDataSocket;
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				String received = mineDataSocket.receiveMessage();
				String[] data = received.split(",");
				if (data[0].equals("rpos")) {
					MineRobotClientApplet.robotX = Integer.parseInt(data[1]);
				}
				else if (data[0].equals("mine")) {
					int x = Integer.parseInt(data[1]);
					int y = Integer.parseInt(data[2]);
					synchronized (this) {
						MineRobotClientApplet.mines.add(new Mine(x, y, MineRobotClientApplet.minGrid, MineRobotClientApplet.minGrid));
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}
	
}

class Mine {
	int x;
	int y;
	int width;
	int height;
	
	Mine(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}
