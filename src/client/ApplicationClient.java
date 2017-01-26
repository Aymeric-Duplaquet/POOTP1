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

import common.Commande;
import common.TypeCommande;

public class ApplicationClient {

	private int portNumber=4444;
	private String hostName="";
	
	private BufferedReader commandesReader;
	private BufferedReader resultReader;

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
			if(ligne==null){
				return null;
			}
			commande.setCommande(ligne);
//			fichier.close(); 
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
		String[] tempoSplit = strCmd.split("#",2);
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
			commandesReader=new BufferedReader(ipsrCommandes);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		InputStream ipsSortie;
		try {
			ipsSortie = new FileInputStream(fichSortie);
			InputStreamReader ipsrSortie=new InputStreamReader(ipsSortie);
			resultReader=new BufferedReader(ipsrSortie);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	public void close() throws IOException{
		commandesReader.close();
		resultReader.close();
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

			if(uneCommande.getType()==TypeCommande.lecture || uneCommande.getType()==TypeCommande.fonction ){
				while(true)
				{
					// Lecture des informations en provenance du serveur
					fromServer = inFromServer.readObject();
					if(fromServer!=null) {break;}
				}
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

	 public void scenario() throws ClassNotFoundException {
		    System.out.println("Debut des traitements:");
		    Commande prochaine = saisisCommande(commandesReader);
		    while (prochaine != null) {
		    	System.out.println("\tTraitement de la commande " + prochaine + " ...");
		      Object resultat = traiteCommande(prochaine);
		      System.out.println("\t\tResultat: " + resultat);
		      prochaine = saisisCommande(commandesReader);
		    }
		    System.out.println("Fin des traitements");
		  }

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		ApplicationClient appli = new ApplicationClient(args[0],args[1]);
		appli.initialise(args[2], args[3]);
		appli.scenario();
		appli.close();

	}

}



