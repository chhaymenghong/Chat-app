import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.print.attribute.ResolutionSyntax;


public class ClientSide implements Runnable {
	
	private static Socket socket = null;
	private static PrintWriter outputStream = null;
	private static BufferedReader responseStream = null;
	private static BufferedReader thisUserStream = null;
	private static boolean closed = false;
	public static void main(String[] args) {
		String hostName = "localhost";
		int portNumber = 5001;
		
		System.out.println("Welcome to Menghong's Crappy chat system");
		
		// Establish connection
		try {
			socket = new Socket(hostName, portNumber);
			System.out.println(socket.getInetAddress());
			outputStream = new PrintWriter(socket.getOutputStream(), true);
			responseStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			thisUserStream = new BufferedReader(new InputStreamReader(System.in));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Can not connect");
		}
		
		// Data Communication
		if (socket != null && outputStream != null && responseStream != null && thisUserStream != null) {
			try {
				// One thread is used to communicate with server
				// Another thread is used to deal with standard input
				
				// This thread is created to deal with server response
				new Thread(new ClientSide()).start();
				
				// This thread waits for user input and send it to server
				while (!closed) {
					outputStream.println(thisUserStream.readLine().trim());
					
				}
				
				outputStream.close();
				thisUserStream.close();
				responseStream.close();
				socket.close();
			} catch (IOException e) {
				System.out.println("IOException:  " + e);
			}
		}
	}
	
	// This thread waits to receive messages from server
	public void run() {
		String responseLine;
		try {
			while ((responseLine = responseStream.readLine()) != null) {
				System.out.println(responseLine);
				if (responseLine.indexOf("Bye") != -1) {
					
					break;
				}
			}
		} catch (IOException e) {
			System.out.println("IOException: " + e);
		}
	}
	
	
}
