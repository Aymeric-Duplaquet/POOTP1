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
	
	// Port de communication pour les Sockets.
	private int portNumber=4444;
	// Nom de la machine hébergeant le serveur.
	private String hostName="";
	
	// BufferdReader relatif au fichier Commandes.txt.
	private BufferedReader commandesReader;
	// BufferdReader relatif au fichier Resultats.txt.
	private BufferedReader resultReader;

	// Ici l'entrée "port" est un String et non un Integer car la classe "main" prend en entrée un tableau de String.
	public ApplicationClient(String hostname,String port){
		this.portNumber=Integer.parseInt(port);
		this.hostName=hostname;
	}

	// Prend en entrée un fichier contenant une liste de commandes et charge une commande
	// dans une variable retournée de type Commande.
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

	// Prend en entrée une Commande, associe un type à la Commande en discriminant le mot avant le #,
	// puis retourne la même Commande qu'en entrée sans "xxxxx#".
	// La commande en entrée "xxxxx#yyyyy" devient en sortie "yyyyy" avec le type "xxxxx".
	public Commande discriminationCommande (Commande commande){


		String strCmd = commande.getCommande();
		// La Commande est split en deux parties grâce au #.
		String[] tempoSplit = strCmd.split("#",2);
		// On ne s'intéresse ici qu'à la première partie pour faire une comparaison avec les Commandes existantes
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
		
		// On retourne seulement la deuxième partie issue du split.
		commande.setCommande(tempoSplit[1]);
		return commande;
	}

	// Ouvre les fichiers "Commandes.txt" et "Resultats.txt".
	// Les ouvertures sont stockées dans deux BufferedReader privés.
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

	// Ferme les fichiers "Commandes.txt" et "Resultats.txt".
	public void close() throws IOException{
		commandesReader.close();
		resultReader.close();
	}

	// Prend en entrée une Commande et la fait exécuter par le serveur. Le résultat de l’exécution est retournée par le serveur.
	// Si la commande ne retourne pas de résultat, on retourne null.
	// Chaque appel ouvre, exécute et ferme une connexion.
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

	// Méthode de test
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

	 // Prend 4 arguments: 1) “hostname” du serveur, 2) numéro de port,3) nom du fichier de commandes, et 4) nom du fichier de sortie.
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		//Créer une instance de ApplicationClient.
		ApplicationClient appli = new ApplicationClient(args[0],args[1]);
		// Ouverture des fichiers.
		appli.initialise(args[2], args[3]);
		// Exécution du scénario.
		appli.scenario();
		// Fermeture des fichiers.
		appli.close();

	}

}



