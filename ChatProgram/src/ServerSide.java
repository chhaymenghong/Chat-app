import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerSide {
	private static ServerSocket serverSocket = null;
	private static Socket clientSocket = null;
	private static final int maxClientsCount = 10;
	private static final clientThread[] threads = new clientThread[maxClientsCount];
	private static final int portNumber = 5001;
	public static void main(String[] args) {
		// Server sockets is only for listening to new connection
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Can't connect to port");
		}
		
		// Create a client socket for each connection
		// Push it to new clientThread
		while (true) {
			try {
				// accept connection from users and listen to each of them on different port
				clientSocket = serverSocket.accept();
				int i;
				// Choosing the available threads to open connection for new chat user
				// clientSocket can write to and read from client
				for (i = 0; i < maxClientsCount; i++) {
					if (threads[i] == null) {
						threads[i] = new clientThread(clientSocket, threads);
						threads[i].start();
						break;
					}
				}
				// Can only accept 10 clients
				if (i == maxClientsCount) {
					PrintStream os = new PrintStream(clientSocket.getOutputStream());
					os.println("Server too popular. Try later.");
					os.close();
					clientSocket.close();
		        }
		      } catch (IOException e) {
		        System.out.println("IOException " + e);
		      }
	    }
	}
}

// Server handle all the clients and broadcast all the message to all the user
// This clientThread opens input and output stream for each client
class clientThread extends Thread {
	 private BufferedReader is = null;
	 private PrintWriter os = null;
	 private Socket clientSocket = null;
	 private final clientThread[] threads;
	 private int maxClientsCount;
	 
	 public clientThread(Socket clientSocket, clientThread[] threads) {
		 this.clientSocket = clientSocket;
	     this.threads = threads;
	     maxClientsCount = threads.length;
	  }
	 
	 public void run() {
		 try {
			 // Create input and output streams for this particular client
			 // Read from client
			 is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			 // write to client
			 os = new PrintWriter(clientSocket.getOutputStream(), true);
			 // Communicate to that client specifically
			 os.println("Enter your name.");
			 // waiting for response from that client to enter his name
			 String name = is.readLine().trim();
			 os.println("Welcome " + name + " to Menghong' s freaking awesome chat room.\nTo leave enter /quit in a new line");
			 
			 // Tell all the users except himself that this new player just entered
			 for (int i = 0; i < maxClientsCount; i++) {
				 if (threads[i] != null && threads[i] != this) {
			          threads[i].os.println("*** A new player " + name
			              + " entered the chat room !!! ***");
				 }
			 }
			 
			 // Client communicates to here 
			 // and broadcast to the rest including himself
			 while (true) {
				 String message = is.readLine();
				 if (message.startsWith("/quit")) {
					 break;
				 }
				 // broadcast too all users including himself
				 for (int i = 0; i < maxClientsCount; i++) {
					 if (threads[i] != null) {
		             threads[i].os.println(name + ": " + message);
		          }
		        }
			 }
			 
			 // Let other users know that this client is leaving the chat room
			 for (int i = 0; i < maxClientsCount; i++) {
		        if (threads[i] != null && threads[i] != this) {
		        	threads[i].os.println("*** The user " + name
		            + " is leaving the chat room !!! ***");
		        }
		     }
			 
			 os.println("*** Bye " + name + " ***");
			 
			 // Reset this thead
			 for (int i = 0; i < maxClientsCount; i++) {
		        if (threads[i] == this) {
		          threads[i] = null;
		        }
		     }
			 
			 is.close();
		     os.close();
		     clientSocket.close();
		 } catch (IOException e) {
			 System.out.println("Something went wrong");
		 }
	 }
}
