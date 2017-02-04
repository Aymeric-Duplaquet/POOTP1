package client;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import common.Commande;
import common.Resultat;
import common.TypeCommande;

public class ApplicationClient {
	
	// Port de communication pour les Sockets.
	private int portNumber=4444;
	// Nom de la machine hÃ©bergeant le serveur.
	private String hostName="";
	
	// BufferdReader relatif au fichier Commandes.txt.
	private BufferedReader commandesReader;
	// BufferdReader relatif au fichier Resultats.txt.
	private BufferedWriter resultWriter;

	// Ici l'entrÃ©e "port" est un String et non un Integer car la classe "main" prend en entrÃ©e un tableau de String.
	public ApplicationClient(String hostname,String port){
		this.portNumber=Integer.parseInt(port);
		this.hostName=hostname;
	}

	// Prend en entrÃ©e un fichier contenant une liste de commandes et charge une commande
	// dans une variable retournÃ©e de type Commande.
	public Commande saisisCommande(BufferedReader fichier) throws ClassNotFoundException {
		
		Commande commande = new Commande();
		
		try{
			String ligne=fichier.readLine();
			if(ligne==null){
				return null;
			}
			commande.setCommande(ligne);
		}		
		catch (Exception e){
			System.out.println(e.toString());
		}
		System.out.println(commande.getCommande());
		commande = discriminationCommande(commande);
		System.out.println(commande.getCommande());
		return commande;
	}

	// Prend en entrÃ©e une Commande, associe un type Ã  la Commande en discriminant le mot avant le #,
	// puis retourne la mÃªme Commande qu'en entrÃ©e sans "xxxxx#".
	// La commande en entrÃ©e "xxxxx#yyyyy" devient en sortie "yyyyy" avec le type "xxxxx".
	public Commande discriminationCommande (Commande commande){


		String strCmd = commande.getCommande();
		// La Commande est split en deux parties grÃ¢ce au #.
		String[] tempoSplit = strCmd.split("#",2);
		// On ne s'intÃ©resse ici qu'Ã  la premiÃ¨re partie pour faire une comparaison avec les Commandes existantes
		// et associer le bon type.
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
		
		// On retourne seulement la deuxiÃ¨me partie issue du split.
		commande.setCommande(tempoSplit[1]);
		return commande;
	}

	// Ouvre les fichiers "Commandes.txt" et "Resultats.txt".
	// Les ouvertures sont stockÃ©es dans deux BufferedReader privÃ©s.
	public void initialise(String fichCommandes, String fichSortie) {
		
		//Ouverture de "Commandes.txt".
		InputStream ipsCommandes;
		try {
			ipsCommandes = new FileInputStream(fichCommandes);
			InputStreamReader ipsrCommandes=new InputStreamReader(ipsCommandes);
			commandesReader=new BufferedReader(ipsrCommandes);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		//Ouverture de "Resultats.txt".
		OutputStream ipsSortie;
		try {
			ipsSortie = new FileOutputStream(fichSortie);
			OutputStreamWriter ipsrSortie=new OutputStreamWriter(ipsSortie);
			resultWriter=new BufferedWriter(ipsrSortie);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	// Ferme les fichiers "Commandes.txt" et "Resultats.txt".
	public void close() throws IOException{
		commandesReader.close();
		resultWriter.close();
	}

	// Prend en entrÃ©e une Commande et la fait exÃ©cuter par le serveur. Le rÃ©sultat de lâ€™exÃ©cution est retournÃ©e par le serveur.
	// Si la commande ne retourne pas de rÃ©sultat, on retourne null.
	// Chaque appel ouvre, exÃ©cute et ferme une connexion.
	public Object traiteCommande(Commande uneCommande) throws ClassNotFoundException {
		Object fromServer = null;

		try (
				Socket kkSocket = new Socket(hostName, portNumber);
				// Pour Ã©crire des informations Ã  destination du serveur.
				ObjectOutputStream  outToServer  = new ObjectOutputStream (kkSocket.getOutputStream());
				// Pour lire les informations en provenance du serveur.
				ObjectInputStream  inFromServer  = new ObjectInputStream (kkSocket.getInputStream());
				) {
			outToServer.writeObject(uneCommande); 

			while((fromServer = inFromServer.readObject()) == null)
			{
				
			}
			
			if(uneCommande.getType()==TypeCommande.lecture || uneCommande.getType()==TypeCommande.fonction ){
				resultWriter.write(fromServer.toString());
				resultWriter.write("\n");
			}
			
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " +
					hostName);
			System.exit(1);
		}
		System.out.println(fromServer.toString());
		return fromServer;
	}

	// MÃ©thode de test
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

	 // Prend 4 arguments: 1) â€œhostnameâ€� du serveur, 2) numÃ©ro de port,3) nom du fichier de commandes, et 4) nom du fichier de sortie.
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		//CrÃ©er une instance de ApplicationClient.
		ApplicationClient appli = new ApplicationClient(args[0],args[1]);
		// Ouverture des fichiers.
		appli.initialise(args[2], args[3]);
		// ExÃ©cution du scÃ©nario.
		appli.scenario();
		// Fermeture des fichiers.
		appli.close();

	}

}



