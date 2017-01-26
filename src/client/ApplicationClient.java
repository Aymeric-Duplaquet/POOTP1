package client;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import common.Commande;
import common.TypeCommande;

public class ApplicationClient {

	private int portNumber=4444;
	private String hostName="";
//	private BufferedReader bufferFichierEntre;

	public ApplicationClient(String hostname,String port){
		this.portNumber=Integer.parseInt(port);
		this.hostName=hostname;
	}


	public Commande saisisCommande(BufferedReader fichier) throws ClassNotFoundException {
		Commande commande = new Commande();
		try{
			/*
			while ((ligne=fichier.readLine())!=null){
				commande.setCommande(ligne);
			}
			*/
			String ligne=fichier.readLine();
			commande.setCommande(ligne);
			fichier.close(); 
		}		
		catch (Exception e){
			System.out.println(e.toString());
		}
		System.out.println(commande.getCommande());
		commande = discriminationCommande(commande);
		System.out.println(commande.getCommande());
		traiteCommande(commande);
		return commande;
	}
	
	public Commande discriminationCommande (Commande commande){
		

		String strCmd = commande.getCommande();
		String[] tempoSplit = strCmd.split("#");
		String commandeTypeStr = tempoSplit[0];

		if(commandeTypeStr.compareTo("compilation")==0)
		{
			commande.setType(TypeCommande.compilation);
		}
		else if(commandeTypeStr.compareTo("chargement")==0)
		{
			commande.setType(TypeCommande.chargement);
		}
		else if(commandeTypeStr.compareTo("creation")==0)
		{
			commande.setType(TypeCommande.creation);
		}
		else if(commandeTypeStr.compareTo("lecture")==0)
		{
			commande.setType(TypeCommande.lecture);
		}
		else if(commandeTypeStr.compareTo("ecriture")==0)
		{
			commande.setType(TypeCommande.ecriture);
		}
		else if(commandeTypeStr.compareTo("fonction")==0)
		{
			commande.setType(TypeCommande.fonction);
		}
		else
		{
			System.out.println("Commande non reconnue");
		}
		
		commande.setCommande(tempoSplit[1]);
		return commande;
	}

	
	public void initialise(String fichCommandes, String fichSortie) {
		InputStream ipsCommandes;
		try {
			ipsCommandes = new FileInputStream(fichCommandes);
			InputStreamReader ipsrCommandes=new InputStreamReader(ipsCommandes);
			//			BufferedReader brCommandes=new BufferedReader(ipsrCommandes);
			//	    	appli.saisisCommande(br);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		InputStream ipsSortie;
		try {
			ipsSortie = new FileInputStream(fichSortie);
			InputStreamReader ipsrSortie=new InputStreamReader(ipsSortie);
			//			BufferedReader brSortie=new BufferedReader(ipsrSortie);
			//	    	appli.saisisCommande(br);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public Object traiteCommande(Commande uneCommande) throws ClassNotFoundException {
		Object fromServer = null;

		try (
				Socket kkSocket = new Socket(hostName, portNumber);
				// Pour écrire des informations à destination du serveur.
				ObjectOutputStream  outToServer  = new ObjectOutputStream (kkSocket.getOutputStream());
				// Pour lire les informations en provenance du serveur.
				ObjectInputStream  inFromServer  = new ObjectInputStream (kkSocket.getInputStream());
				) {
			outToServer.writeObject(uneCommande); 


			while(true)
			{
				// Lecture des informations en provenance du serveur
				fromServer = inFromServer.readObject();
				if(fromServer!=null) {break;}
			}

		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " +
					hostName);
			System.exit(1);
		}
		System.out.println((String)fromServer);
		return fromServer;
	}

	 /* public void scenario() {
		    System.out.println("Debut des traitements:");
		    Commande prochaine = saisisCommande(commandesReader);
		    while (prochaine != null) {
		    	System.out.println("\tTraitement de la commande " + prochaine + " ...");
		      Object resultat = traiteCommande(prochaine);
		      System.out.println("\t\tResultat: " + resultat);
		      prochaine = saisisCommande(commandesReader);
		    }
		    System.out.println("Fin des traitements");
		  }*/

	public static void main(String[] args) throws FileNotFoundException, ClassNotFoundException {
		ApplicationClient appli = new ApplicationClient(args[0],args[1]);
		InputStream ipsCommandes = new FileInputStream("src/client/Commandes.txt");
		InputStreamReader ipsrCommandes=new InputStreamReader(ipsCommandes);
		BufferedReader brCommandes=new BufferedReader(ipsrCommandes);
		appli.saisisCommande(brCommandes);
		
	}

}



