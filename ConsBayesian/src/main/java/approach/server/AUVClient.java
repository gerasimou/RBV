package approach.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.commons.math3.distribution.ExponentialDistribution;

public class AUVClient {

	public static void main(String[] args) {
		String serverAddress 			= "127.0.0.1";
		int serverPort       			= 8860;
		
		double r_fail_clean = 0.95/60.0;
		double r_damage     = 0.00001/60.0;
		double r_clean      = (1-0.95-0.00001)/60.0;
		ExponentialDistribution dirtyExponential = new ExponentialDistribution(1/(r_damage + r_clean + r_fail_clean));

		double dirtyHoldingTSum = 0;
	    int dirtyHoldingTCount 	= 0;

		Socket socket;
		try {
			socket = new Socket(serverAddress, serverPort);

			BufferedReader inFromServer 	= new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter outToServer			= new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
		
			for (int i=0; i<10; i++) {			
	            double holdingT		= dirtyExponential.sample();
	            dirtyHoldingTSum   += holdingT;
	            dirtyHoldingTCount++;
	            String outputString = "6,1,"+ dirtyHoldingTCount +","+ dirtyHoldingTSum;
				System.out.println("Sending:\t" + outputString);
				outToServer.println(outputString);
				outToServer.flush();
				//read from server
				String response = inFromServer.readLine();
				System.out.println("Received:\t" + response);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
