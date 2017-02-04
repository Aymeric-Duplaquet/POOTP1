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
	
	// Nom de la machine hebergeant le serveur.
	private String hostName="";

	// BufferdReader relatif au fichier Commandes.txt.
	private BufferedReader commandesReader;
	
	// BufferdReader relatif au fichier Resultats.txt.
	private BufferedWriter resultWriter;

	// Ici l'entree "port" est un String et non un Integer car la classe "main" prend en entree un tableau de String.
	public ApplicationClient(String hostname,String port){
		this.portNumber=Integer.parseInt(port);
		this.hostName=hostname;
	}

	// Prend en entree un fichier contenant une liste de commandes et charge une commande
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
			commande.setType(TypeCommande.Compilation);
		}
		else if(commandeTypeStr.compareTo("chargement")==0)
		{
			commande.setType(TypeCommande.Chargement);
		}
		else if(commandeTypeStr.compareTo("creation")==0)
		{
			commande.setType(TypeCommande.Creation);
		}
		else if(commandeTypeStr.compareTo("lecture")==0)
		{
			commande.setType(TypeCommande.Lecture);
		}
		else if(commandeTypeStr.compareTo("ecriture")==0)
		{
			commande.setType(TypeCommande.Ecriture);
		}
		else if(commandeTypeStr.compareTo("fonction")==0)
		{
			commande.setType(TypeCommande.Fonction);
		}
		else
		{
			System.out.println("Commande non reconnue");
		}

		// On retourne seulement la deuxieme partie issue du split.
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

	// Prend en entrÃ©e une Commande et la fait executer par le serveur. Le resultat execute est retournee par le serveur.
	// Si la commande ne retourne pas de resultat, on retourne null.
	// Chaque appel ouvre, execute et ferme une connexion.
	public Object traiteCommande(Commande uneCommande) throws ClassNotFoundException {
		Object fromServer = null;

		try (
				Socket kkSocket = new Socket(hostName, portNumber);
				// Pour ecrire des informations a  destination du serveur.
				ObjectOutputStream  outToServer  = new ObjectOutputStream (kkSocket.getOutputStream());
				// Pour lire les informations en provenance du serveur.
				ObjectInputStream  inFromServer  = new ObjectInputStream (kkSocket.getInputStream());
				) {
			outToServer.writeObject(uneCommande); 

			while((fromServer = inFromServer.readObject()) == null)
			{

			}
				// On détecte la reponse du serveur en fonction du Resultat recu pour savoir si il y a eu un probleme
				// Si ce n est pas le cas, alors confirme une commande traitee ou envoie un retour traite.
				Resultat ResultatFromServeur=(Resultat) fromServer;
				if(ResultatFromServeur.getProblem()==false){
					String resultat = fromServer.toString();
					if (resultat.compareTo("")==0){
						resultWriter.write("Le serveur a pris en compte "+uneCommande.getType().toString());
					}else{
						resultWriter.write(resultat);
					}
				}else{
					resultWriter.write("Problème détecté");
				}
				resultWriter.newLine();
				
			

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



