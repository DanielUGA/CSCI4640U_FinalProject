package ca.uoit.Allen.Matt.DSFinal;

import java.io.*;
import java.net.*;
import java.util.*;

public class MineRobot {
	
	private static final int ROBOT_AUTH = 1;
	public static boolean MOVING = false;
	public static int distance = 0;
	public static int robotX = -10;
	public static final int SENSOR_NUMBER = 50;
	public static String hostname = "localhost";
	public static int serverPort = 3333;
	static BufferedReader stdIn;
	static MineStreamSocket mineDataSocket;
	public static int SLEEP_TIME = 200;
	
	public static void main(String[] args) {
		stdIn = new BufferedReader(new InputStreamReader(System.in));
		if (args.length == 1)
			serverPort = Integer.parseInt(args[0]);
		if (args.length == 2) {
			hostname = args[0];
			serverPort = Integer.parseInt(args[1]);
		}
		
		try {
			mineDataSocket = new MineStreamSocket(InetAddress.getByName(hostname), serverPort);
			mineDataSocket.sendMessage(String.valueOf(ROBOT_AUTH));
			while (true) {
				Thread moveRobot = new Thread(new MovingRobot(mineDataSocket));
				ArrayList<Thread> sensorThreads = new ArrayList<Thread>();
				for(int i = 0; i < SENSOR_NUMBER; i++) {
					sensorThreads.add(new Thread(new SensorThread(mineDataSocket, i*10)));
				}
				String received = mineDataSocket.receiveMessage();
				String[] data = received.split(",");
				if (data[0].equals("distance")) {
					distance = Integer.parseInt(data[1]);
				}
				else if (data[0].equals("start")) {
					MOVING = true;
					moveRobot.start();
					for(Thread t : sensorThreads) {
						t.start();
					}
				}
				else if (data[0].equals("end")) {
					moveRobot.stop();
					for(Thread t : sensorThreads) {
						t.stop();
					}
				}
			}
		} catch(Exception e) {
			System.out.println("Lost connection to the server.");
		}
	}
}

// thread that controls the movement of the robot.
class MovingRobot implements Runnable {

	MineStreamSocket mineDataSocket;
	
	MovingRobot(MineStreamSocket mineDataSocket) {
		this.mineDataSocket = mineDataSocket;
	}
	
	@Override
	public void run() {
		while(true) {
			if (MineRobot.MOVING) {
				MineRobot.robotX += 2;
				try {
					mineDataSocket.sendMessage("rpos,"+MineRobot.robotX);
					if (MineRobot.robotX > MineRobot.distance) {
						MineRobot.MOVING = false;
						System.out.println("Robot has reached destination! Powering Off...");
						mineDataSocket.close();
						System.exit(0);
						break;
					}
						
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(MineRobot.SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}	
}

// robot sensor thread, controls when to fire off an event
// when a mine is found.
class SensorThread implements Runnable {

	MineStreamSocket mineDataSocket;
	int yGrid;
	
	SensorThread(MineStreamSocket mineDataSocket, int yGrid) {
		this.mineDataSocket = mineDataSocket;
		this.yGrid = yGrid;
	}
	
	public void run() {
		while(true) {
			synchronized (this) {
				if (MineRobot.MOVING && (MineRobot.robotX % 10 == 0)) {
					Random r = new Random();
					int chance = r.nextInt(1000);
					if (chance < 100) {
						try {
							mineDataSocket.sendMessage("mine,"+MineRobot.robotX+","+yGrid);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				try {
					Thread.sleep(MineRobot.SLEEP_TIME-100); // unlock loop
				} catch (InterruptedException e) {
					e.printStackTrace();
			}
			}
		}
	}
	
}