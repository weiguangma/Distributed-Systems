package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
/**
 * 
 * @author Weiguang Ma
 *
 */
public class Server {

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Number of parameters incorrect");
			System.exit(0);
		}

		ServerWindow window = new ServerWindow();
		window.frame.setVisible(true);

		ServerSocket listeningSocket = null;
		int ROOMSIZE = 3;
		ArrayList<Room> room = new ArrayList<Room>();

		for (int i = 0; i < ROOMSIZE; i++) {
			room.add(new Room(i));
		}
		AllPlayer registration = new AllPlayer();

		try {

			int port = Integer.parseInt(args[0]);
			listeningSocket = new ServerSocket(port);
			window.textArea.append(Thread.currentThread().getName() + " - Server listening on port "
					+ port + " for a connection\n");

			// Keep track of the number of clients
			int clientNum = 0;

			// Listen for incoming connections for ever
			while (true) {

				// Accept an incoming client connection request
				Socket clientSocket = listeningSocket.accept();
				window.textArea.append(
						Thread.currentThread().getName() + " - Client conection accepted\n");
				clientNum++;

				// Create one thread per client connection, each thread will be
				// responsible for listening for messages from the client
				// and then 'handing' them to the client manager (coordinating singleton)
				// to process them
				ClientConnection clientConnection = new ClientConnection(clientSocket, clientNum,
						room, registration, window);
				clientConnection.setName("Player" + clientNum);
				clientConnection.start();

				// Register the new client connection with the client manager
				ClientManager.getInstance().clientConnected(clientConnection);

			}
		} catch (SocketException e) {
			System.out.println("Port is occupied, please try again");
			System.exit(0);
		} catch (IOException e) {
			System.out.println("You must input valid parameters");
			System.exit(0);
		} catch (NumberFormatException e) {
			System.out.println("You must input a valid port number");
			System.exit(0);
		} finally {
			if (listeningSocket != null) {
				try {
					listeningSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
