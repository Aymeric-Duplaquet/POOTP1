package serveur;
import java.net.*;
import java.io.*;

import common.TempoKnockKnockProtocole;


public class Serveur {
public static void main(String[] args) throws IOException {
        
        if (args.length != 1) {
            System.err.println("Usage: java KnockKnockServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        
        try ( 
            ServerSocket serverSocket = new ServerSocket(portNumber);
        		// En attente d'une requête par le client.
            Socket clientSocket = serverSocket.accept();
        		// Pour écrire des informations à destination du client.
            PrintWriter out =
                new PrintWriter(clientSocket.getOutputStream(), true);
        		// Pour lire les informations en provenance du client.
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        ) {
        	System.out.println("Serveur Ready");
            String inputLine, outputLine;
            
            // Initiate conversation with client
            TempoKnockKnockProtocole kkp = new TempoKnockKnockProtocole();
            outputLine = kkp.processInput(null);
            out.println(outputLine);
            while ((inputLine = in.readLine()) != null) {
                outputLine = kkp.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye."))
                    break;
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
