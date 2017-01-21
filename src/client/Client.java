package client;
import java.net.*;
import java.util.LinkedList;
import java.io.*;

import common.Commande;


public class Client {
	//Argument : hostname = PC name ; port = Server port, ex : 4444
	 public static void main(String[] args) throws IOException, ClassNotFoundException {
	        
	        if (args.length != 2) {
	            System.err.println(
	                "Usage: java EchoClient <host name> <port number>");
	            System.exit(1);
	        }
	        
	        String hostName = args[0];
	        int portNumber = Integer.parseInt(args[1]);

	        try (
	            Socket kkSocket = new Socket(hostName, portNumber);
	        		// Pour écrire des informations à destination du serveur.
	        		ObjectOutputStream  outToServer  = new ObjectOutputStream (kkSocket.getOutputStream());
	        		// Pour lire les informations en provenance du serveur.
	        		ObjectInputStream  inFromServer  = new ObjectInputStream (kkSocket.getInputStream());
	        ) {
	        	// Ce qui est écrit par l'utilisateur dans la console.
	            BufferedReader stdIn =
	                new BufferedReader(new InputStreamReader(System.in));
	            Object fromServer;
	            Object fromUser;
	            
	            LinkedList<Commande> msgList = new LinkedList<>();
	            Commande msg = new Commande();
	            msg.setCommande("HeHo");
	            msgList.push(msg);
	            
	            outToServer.writeObject(msg); 
	            
	            while(true)
	            {
	            	fromServer = inFromServer.readObject();
	            	if(fromServer!=null)
	            	{
	            		Commande inCmd = (Commande) fromServer;
	            		System.out.println("Client : "+ inCmd.getCommande());
	            		break;
	            	}
	            }

	            /*
	            // Lecture des informations en provenance du serveur.
	            while ((fromServer = inFromServer.readObject()) != null) {
	                System.out.println("Server: " + fromServer);
	                if (fromServer.equals("Bye."))
	                    break;
	                // Les informations écrites par l'utilisateur sont stockées dans fromUser.
	                fromUser = stdIn.readLine();
	                if (fromUser != null) {
	                    System.out.println("Client: " + fromUser);
	                    // Envoie des informations écrites par l'utilisateur vers le serveur.
	                    outToServer.writeObject(fromUser);
	                }
	            }
	            */
	        } catch (UnknownHostException e) {
	            System.err.println("Don't know about host " + hostName);
	            System.exit(1);
	        } catch (IOException e) {
	            System.err.println("Couldn't get I/O for the connection to " +
	                hostName);
	            System.exit(1);
	        }
	    }
}
