package serveur;
import java.net.*;
import java.io.*;

import common.Commande;
import common.TempoKnockKnockProtocole;


public class Serveur {

	private ServerSocket serverSocket;

	//Tempo main pour test
	public static void main(String[] args) throws IOException
	{
		int port = Integer.parseInt(args[0]);
		Serveur serveur = new Serveur(port);
		
		while(true)
		{
			try 
			{
				serveur.aVosOrdres();
			} catch (Exception e) 
			{
				e.printStackTrace();
				break;
			}
		}


		/*Commande testCmd = new Commande();
		testCmd.setCommande("compilation#chemin_relatif_du_fichier_source_1");
		test.traiteCommande(testCmd);
		 */

	}

	/**
	 * prend le num�ro de port, cr�e un SocketServer sur le port
	 * @throws IOException 
	 */
	public Serveur (int port) throws IOException 
	{
		serverSocket = new ServerSocket(port);
	}

	/**
	 * Se met en attente de connexions des clients. Suite aux connexions, elle lit
	 * ce qui est envoy� � travers la Socket, recr�e l�objet Commande envoy� par
	 * le client, et appellera traiterCommande(Commande uneCommande)
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */         
	public void aVosOrdres() throws IOException, ClassNotFoundException 
	{

		Socket clientSocket = serverSocket.accept();
		// Pour écrire des informations à destination du client.
		ObjectOutputStream  outToClient  = new ObjectOutputStream(clientSocket.getOutputStream());
		// Pour lire les informations en provenance du client.
		ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());

		Object inputObject;
		Commande inCmd;
		
		
		while(true)
		{
			inputObject = inFromClient.readObject();
			if(inputObject!=null)
			{
				inCmd = (Commande) inputObject;
				break;
			}
		}
		System.out.println("Serveur : "+ inCmd.getCommande());
		traiteCommande(inCmd);

	}

	/**
	 * prend uneCommande dument formatt�e, et la traite. D�pendant du type de commande, 
	 * elle appelle la m�thode sp�cialis�e
	 */
	public void traiteCommande(Commande uneCommande) 
	{
		String strCmd = uneCommande.getCommande();
		String commandeTypeStr;
		String[] tempoSplit = strCmd.split("#");
		commandeTypeStr = tempoSplit[0];

		if(commandeTypeStr.compareTo("compilation")==0)
		{
			System.out.println("Commande compilation detect�");
		}
		else if(commandeTypeStr.compareTo("chargement")==0)
		{
			System.out.println("Commande chargement detect�");
		}
		else if(commandeTypeStr.compareTo("creation")==0)
		{
			System.out.println("Commande creation detect�");
		}
		else if(commandeTypeStr.compareTo("lecture")==0)
		{
			System.out.println("Commande lecture detect�");
		}
		else if(commandeTypeStr.compareTo("ecriture")==0)
		{
			System.out.println("Commande ecriture detect�");
		}
		else if(commandeTypeStr.compareTo("fonction")==0)
		{
			System.out.println("Commande fonction detect�");
		}
		else
		{
			System.out.println("Commande non reconnue");
		}
	}

	/**
	 * traiterLecture : traite la lecture d�un attribut. Renvoies le r�sultat par le 
	 * socket
	 */
	public void traiterLecture(Object pointeurObjet, String attribut) 
	{

	}

	/**
	 * traiterEcriture : traite l��criture d�un attribut. Confirmes au client que l��criture
	 * s�est faite correctement.
	 */
	public void traiterEcriture(Object pointeurObjet, String attribut, Object valeur) 
	{

	}

	/**
	 * traiterCreation : traite la cr�ation d�un objet. Confirme au client que la cr�ation
	 * s�est faite correctement.
	 */
	public void traiterCreation(Class classeDeLobjet, String identificateur) 
	{

	}

	/**
	 * traiterChargement : traite le chargement d�une classe. Confirmes au client que la cr�ation
	 * s�est faite correctement.
	 */
	public void traiterChargement(String nomQualifie) 
	{

	}

	/**
	 * traiterCompilation : traite la compilation d�un fichier source java. Confirme au client
	 * que la compilation s�est faite correctement. Le fichier source est donn� par son chemin
	 * relatif par rapport au chemin des fichiers sources.
	 */
	public void traiterCompilation(String cheminRelatifFichierSource) 
	{

	}

	/**
	 * traiterAppel : traite l�appel d�une m�thode, en prenant comme argument l�objet
	 * sur lequel on effectue l�appel, le nom de la fonction � appeler, un tableau de nom de 
	 * types des arguments, et un tableau d�arguments pour la fonction. Le r�sultat de la 
	 * fonction est renvoy� par le serveur au client (ou le message que tout s�est bien 
	 * pass�)
	 **/
	public void traiterAppel(Object pointeurObjet, String nomFonction, String[] types,Object[] valeurs) 
	{

	}


}
