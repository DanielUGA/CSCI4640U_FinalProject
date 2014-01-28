package ca.uoit.Allen.Matt.DSFinal;

import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

public class MineRobotServer {
	
	public static boolean CLIENT_CONNECTED = false;
	public static boolean ROBOT_CONNECTED = false;
	public static boolean SIMULATION_START = false;
	public static Object SYNCHRONIZER = new Object();
	public static ArrayList<MineModel> foundMines;
	public static final String DRIVER_CLASS = "com.mysql.jdbc.Driver";
	public static final String URL = "jdbc:mysql://www.kaganovsky.ca/allen_minerobot";
	public static final String USER = "allen_minerobot";
	public static final String PASSWORD = "mineRobot2";
	
	public static void main(String[] args) {
		
		// assign server port for robot and client to connect to.
		int serverPort = 3333;
		if (args.length == 1)
			serverPort = Integer.parseInt(args[0]);
		foundMines = new ArrayList<MineModel>();
		try {
			ServerSocket serverConnectionSocket = new ServerSocket(serverPort);
			System.out.println("Mine Robot Server Initialized, waiting for Robot and Client connection on port: " + serverPort);
			
			// initialize simulation thread and wait for both active clients.
			Thread mineSimulationThread = new Thread(new MineSimulationThread());
			mineSimulationThread.start();
			
			// get external connections and assign to appropriate thread handler.
			while (true) {
				MineStreamSocket mineDataSocket = new MineStreamSocket(serverConnectionSocket.accept());
				
				Thread clientBouncerThread = new Thread(new ClientBouncerThread(mineDataSocket));
				clientBouncerThread.start();
			}
		} catch (IOException e) {
			System.out.println("Server Connection Socket Error.");
		}
	}

}

// thread passes off connection to the appropriate thread handler.
class ClientBouncerThread implements Runnable {

	private final int UNKNOWN_AUTH = 0;
	private final int ROBOT_AUTH = 1;
	private final int CLIENT_AUTH = 2;
	MineStreamSocket mineDataSocket;
	
	ClientBouncerThread(MineStreamSocket mineDataSocket) {
		this.mineDataSocket = mineDataSocket;
	}
	
	@Override
	public void run() {
		System.out.println("Server waiting for client type broadcast...");
		int type = UNKNOWN_AUTH;
		try {
			type = Integer.parseInt(mineDataSocket.receiveMessage());
			switch(type) {
			case ROBOT_AUTH:
				if (MineRobotServer.ROBOT_CONNECTED == false) {
					System.out.println("Robot Connection Detected!");
					MineRobotServer.ROBOT_CONNECTED = true;
					System.out.println("Starting Robot Connection Thread.");
					Thread rct = new Thread(new RobotConnectionThread(mineDataSocket));
					rct.start();
				} else {
					System.out.println("Robot already connected!");
				}
				break;
			case CLIENT_AUTH:
				if (MineRobotServer.CLIENT_CONNECTED == false) {
					System.out.println("Client Connection Detected!");
					MineRobotServer.CLIENT_CONNECTED = true;
					System.out.println("Starting Client Connection Thread.");
					Thread cct = new Thread(new ClientConnectionThread(mineDataSocket));
					cct.start();
				} else {
					System.out.println("Client already connected!");
				}
				break;
				default:
			}
		} catch(IOException e) {
			System.out.println("Remote Connection to client/robot was lost.");
		}
	}
}

// Thread to handle message passing to the Client connection
class ClientConnectionThread implements Runnable {

	static MineStreamSocket mineDataSocket;
	
	ClientConnectionThread(MineStreamSocket mineDataSocket) {
		this.mineDataSocket = mineDataSocket;
	}
	
	public void run() {
		if (!MineRobotServer.SIMULATION_START) {
			synchronized (MineRobotServer.SYNCHRONIZER) {
				try {
					MineRobotServer.SYNCHRONIZER.wait();
				} catch(InterruptedException e) {
					System.out.println("Interrupted Exception, could not wait thread.");
				}
			}
		}
		while (true) {
			try {
				String cinput = mineDataSocket.receiveMessage();
				RobotConnectionThread.messageRobot(cinput);
			} catch(Exception e) {
				System.out.println("Client disconnected from the server.");
				MineRobotServer.CLIENT_CONNECTED = false;
				MineRobotServer.SIMULATION_START = false;
				break;
			}
		}
	}
	
	public static void messageClient(String message) throws IOException {
		mineDataSocket.sendMessage(message);
	}
}

// Thread to handle message passing to the Robot connection
class RobotConnectionThread implements Runnable {

	static MineStreamSocket mineDataSocket;
	
	RobotConnectionThread(MineStreamSocket mineDataSocket) {
		this.mineDataSocket = mineDataSocket;
	}
	
	public void run() {
		if (!MineRobotServer.SIMULATION_START) {
			synchronized (MineRobotServer.SYNCHRONIZER) {
				try {
					MineRobotServer.SYNCHRONIZER.wait();
				} catch (InterruptedException e) {
					System.out.println("Interrupted Exception, could not wait thread.");
				}
			}
		}
		while(true) {
			try {
				String rinput = mineDataSocket.receiveMessage();
				// intercept message
				String[] data = rinput.split(",");
				if (data[0].equals("mine")) {
					MineRobotServer.foundMines.add(new MineModel(Integer.parseInt(data[1]), Integer.parseInt(data[2])));
				}
				
				ClientConnectionThread.messageClient(rinput);
			} catch(Exception e) {
				System.out.println("Robot disconnected from the server.");
				MineRobotServer.ROBOT_CONNECTED = false;
				MineRobotServer.SIMULATION_START = false;
				
				System.out.println("Update the database");
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < MineRobotServer.foundMines.size(); i++) {
					sb.append(MineRobotServer.foundMines.get(i).x);
					sb.append(',');
					sb.append(MineRobotServer.foundMines.get(i).y);
					sb.append('.');
				}
				sb.deleteCharAt(sb.lastIndexOf("."));
				System.out.println(sb.toString());
				try {
					Class.forName(MineRobotServer.DRIVER_CLASS);
					
					Connection dbc = DriverManager.getConnection(MineRobotServer.URL, MineRobotServer.USER, MineRobotServer.PASSWORD);
					Statement statement = dbc.createStatement();
					statement.execute("INSERT INTO scan (minestring, date) VALUES('"+sb.toString()+"', NOW())");
					System.out.println("Done!");
					statement.close();
					dbc.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				break;
			}
		}
	}
	
	public static void messageRobot(String message) throws IOException {
		mineDataSocket.sendMessage(message);
	}
}

// MineSimulationThread runs at the beginning of the simulation
// and waits until both clients are connected in order to synchronize them
// with the order of message passes between the client and robot connection.
class MineSimulationThread implements Runnable {

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(5000); // check for client and robot connections to be complete every 5 seconds.
				if (MineRobotServer.CLIENT_CONNECTED && MineRobotServer.ROBOT_CONNECTED && !MineRobotServer.SIMULATION_START) {
					System.out.println("Both clients have been detected, starting simulation...");
					MineRobotServer.SIMULATION_START = true;
					synchronized (MineRobotServer.SYNCHRONIZER) {
						MineRobotServer.foundMines.clear();
						MineRobotServer.SYNCHRONIZER.notifyAll(); // notify client/robot threads
					}
				}
			} catch (InterruptedException e) {
				System.out.println("Interrupted Exception, could not sleep thread.");
			}
		}
	}
	
}

class MineModel {
	int x;
	int y;
	
	MineModel(int x, int y) {
		this.x = x;
		this.y = y;
	}
}