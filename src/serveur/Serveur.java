package serveur;
import java.net.*;
import java.util.Hashtable;
import java.io.*;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.sun.corba.se.spi.activation.Server;
import com.sun.corba.se.spi.ior.Identifiable;

import common.Commande;



public class Serveur {

	private ServerSocket serverSocket;
	
	private ObjectInputStream inFromClient;
	private ObjectOutputStream  outToClient;
	
	public Hashtable<String, Object> ident;
	

	//Tempo main pour test
	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		int port = Integer.parseInt(args[0]);
		Serveur serveur = new Serveur(port);
		/*
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
		}*/


		/*Commande testCmd = new Commande();
		testCmd.setCommande("compilation#chemin_relatif_du_fichier_source_1");
		test.traiteCommande(testCmd);
		 */
		serveur.traiterCompilation("./src/ca/uqac/registraire/Cours.java");
		serveur.traiterChargement("ca.uqac.registraire.Cours");
		serveur.traiterCreation(Class.forName("ca.uqac.registraire.Cours"), "Hello");
		
		System.out.println(serveur.ident.get("Hello").toString());
		

	}

	/**
	 * prend le numï¿½ro de port, crï¿½e un SocketServer sur le port
	 * @throws IOException 
	 */
	public Serveur (int port) throws IOException 
	{
		serverSocket = new ServerSocket(port);
		ident = new Hashtable<String,Object>();
	}

	/**
	 * Se met en attente de connexions des clients. Suite aux connexions, elle lit
	 * ce qui est envoyï¿½ ï¿½ travers la Socket, recrï¿½e lï¿½objet Commande envoyï¿½ par
	 * le client, et appellera traiterCommande(Commande uneCommande)
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */         
	public void aVosOrdres() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException 
	{

		Socket clientSocket = serverSocket.accept();
		// Pour Ã©crire des informations Ã  destination du client.
		outToClient  = new ObjectOutputStream(clientSocket.getOutputStream());
		// Pour lire les informations en provenance du client.
		inFromClient = new ObjectInputStream(clientSocket.getInputStream());

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
	 * prend uneCommande dument formattï¿½e, et la traite. Dï¿½pendant du type de commande, 
	 * elle appelle la mï¿½thode spï¿½cialisï¿½e
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public void traiteCommande(Commande uneCommande) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException 
	{
		String strCmd = uneCommande.getCommande();
		String commandeTypeStr;
		String[] listParam = strCmd.split("#");
		
		switch (uneCommande.getType()) {
		case compilation : {
			System.out.println("Commande compilation detectï¿½");
			String[] listChemin = listParam[0].split(",");
			for(int i = 0; i<listChemin.length;i++)
			{
				traiterCompilation(listChemin[i]);
			}
			
			break;
		}
		case chargement : {
			System.out.println("Commande chargement detectï¿½");
			traiterChargement(listParam[0]);
			break;
		}
		case creation : {
			System.out.println("Commande creation detectï¿½");
			traiterCreation(Class.forName(listParam[0]), listParam[1]);
			break;
		}
		case lecture : {
			System.out.println("Commande lecture detectï¿½");
			traiterLecture(ident.get(listParam[0]), listParam[1]);
			break;
		}
		case ecriture : {
			System.out.println("Commande ecriture detectï¿½");
			traiterEcriture(ident.get(listParam[0]), listParam[2], listParam[3]);
			break;
		}
		case fonction : {
			System.out.println("Commande fonction detectï¿½");
			String[] tabListParam = listParam[2].split(",");
			String[] tabTypeParam = new String[listParam.length];
			Object[] tabParam = new Object[listParam.length];
			for(int i = 0;i<tabListParam.length;i++)
			{
				String[] temp = tabListParam[i].split(":");
				tabTypeParam[i] = temp[0];
				tabParam[i] = temp[1];
			}
			traiterAppel(ident.get(listParam[0]), listParam[1], tabTypeParam, tabParam);
			break;
		}
		default:
			System.out.println("Commande non reconnue");
			break;
		}
	}

	/**
	 * traiterLecture : traite la lecture dï¿½un attribut. Renvoies le rï¿½sultat par le 
	 * socket
	 * @throws IOException 
	 */
	public void traiterLecture(Object pointeurObjet, String attribut) throws IOException 
	{
		outToClient.writeObject(new String("Hello from serveur"));
		System.out.println("Réponse envoyé");
	}

	/**
	 * traiterEcriture : traite lï¿½ï¿½criture dï¿½un attribut. Confirmes au client que lï¿½ï¿½criture
	 * sï¿½est faite correctement.
	 */
	public void traiterEcriture(Object pointeurObjet, String attribut, Object valeur) 
	{

	}

	/**
	 * traiterCreation : traite la crï¿½ation dï¿½un objet. Confirme au client que la crï¿½ation
	 * sï¿½est faite correctement.
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void traiterCreation(Class classeDeLobjet, String identificateur) throws InstantiationException, IllegalAccessException 
	{
		Object tempo = classeDeLobjet.newInstance();
		ident.put(identificateur, tempo);
		
		
	}

	/**
	 * traiterChargement : traite le chargement dï¿½une classe. Confirmes au client que la crï¿½ation
	 * sï¿½est faite correctement.
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void traiterChargement(String nomQualifie) throws ClassNotFoundException, InstantiationException, IllegalAccessException 
	{
		ClassLoader classLoad = ClassLoader.getSystemClassLoader();
		Class tempoClass = classLoad.loadClass(nomQualifie);
	}

	/**
	 * traiterCompilation : traite la compilation dï¿½un fichier source java. Confirme au client
	 * que la compilation sï¿½est faite correctement. Le fichier source est donnï¿½ par son chemin
	 * relatif par rapport au chemin des fichiers sources.
	 */
	public void traiterCompilation(String cheminRelatifFichierSource) 
	{
		JavaCompiler compil = ToolProvider.getSystemJavaCompiler();
		System.out.println(compil);
		compil.run(null, null, null, cheminRelatifFichierSource);
		
	}

	/**
	 * traiterAppel : traite lï¿½appel dï¿½une mï¿½thode, en prenant comme argument lï¿½objet
	 * sur lequel on effectue lï¿½appel, le nom de la fonction ï¿½ appeler, un tableau de nom de 
	 * types des arguments, et un tableau dï¿½arguments pour la fonction. Le rï¿½sultat de la 
	 * fonction est renvoyï¿½ par le serveur au client (ou le message que tout sï¿½est bien 
	 * passï¿½)
	 * @throws IOException 
	 **/
	public void traiterAppel(Object pointeurObjet, String nomFonction, String[] types,Object[] valeurs) throws IOException 
	{
		outToClient.writeObject(new String("Hello from serveur"));
		System.out.println("Réponse envoyé");
	}


}
