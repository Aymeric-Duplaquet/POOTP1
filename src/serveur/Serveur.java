package serveur;
import java.net.*;
import java.io.*;

import common.Commande;
import common.TempoKnockKnockProtocole;


public class Serveur {
public static void main(String[] args) throws IOException, ClassNotFoundException {
        
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
        		ObjectOutputStream  outToClient  = new ObjectOutputStream(clientSocket.getOutputStream());
        		// Pour lire les informations en provenance du client.
        		ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());
        		) {

            Object inputObject, outputObject;
            Commande inCmd;
            outputObject= new Object();
            
            // Initiate conversation with client
            //TempoKnockKnockProtocole kkp = new TempoKnockKnockProtocole();
            //outputLine = kkp.processInput(null);
           
            while (true) {
            	//outputObject = kkp.processInput(inputObject);
            	inputObject = inFromClient.readObject();
            	if(inputObject!=null)
            	{
            		inCmd = (Commande) inputObject;
            		System.out.println("Serveur : "+ inCmd.getCommande());
            		break;
            	}
                /*outToClient.writeObject(outputObject);
                if (outputObject.equals("Bye."))
                    break;
                 */
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
