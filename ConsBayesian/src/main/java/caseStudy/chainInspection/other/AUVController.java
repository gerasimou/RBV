package caseStudy.chainInspection.other;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import _main.Property;

public class AUVController {

	public static void main(String[] args) {

		int port		 = 8860;
		String modelFile = "models/auv/auv.sm";
		String propsFile = "models/auv/auv.csl";
		AUVAnalyser auv = new AUVAnalyser(modelFile, propsFile);	

		
		String fromClient;
		String toClient;

		try {
			ServerSocket server = new ServerSocket(port);
			System.out.println("Server ready on port " + port);

			Socket client = server.accept();
			System.out.println("AUV connected on port "+ port);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter out = new PrintWriter(client.getOutputStream(),true);

			while(true) {
				System.out.println("Waiting for information from the AUV");
				fromClient = in.readLine(); //e,g, 6,6,1,83.98852
				System.out.println("Received: " + fromClient);


				String dataFromClient[] = fromClient.split(",");
				int dirtyHoldingTCount	= Integer.parseInt(dataFromClient[2]);
				double dirtyHoldingTSum	= Double.parseDouble(dataFromClient[3]);
				Property[] properties 	= auv.execute(dirtyHoldingTSum, dirtyHoldingTCount);
				
				//TODO: Temporary dummy controller
				if (properties[0].getMin().doubleValue() < 0.9)
					toClient = "0";
				else
					toClient = "1";
				
				System.out.println("Sending (" +properties[0].getMin().doubleValue()+ "): " + toClient);
				out.println(toClient);
				out.flush();
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}