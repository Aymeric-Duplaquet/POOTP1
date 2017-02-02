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
	 * prend le num�ro de port, cr�e un SocketServer sur le port
	 * @throws IOException 
	 */
	public Serveur (int port) throws IOException 
	{
		serverSocket = new ServerSocket(port);
		ident = new Hashtable<String,Object>();
	}

	/**
	 * Se met en attente de connexions des clients. Suite aux connexions, elle lit
	 * ce qui est envoy� � travers la Socket, recr�e l�objet Commande envoy� par
	 * le client, et appellera traiterCommande(Commande uneCommande)
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */         
	public void aVosOrdres() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException 
	{

		Socket clientSocket = serverSocket.accept();
		// Pour écrire des informations à destination du client.
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
	 * prend uneCommande dument formatt�e, et la traite. D�pendant du type de commande, 
	 * elle appelle la m�thode sp�cialis�e
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
			System.out.println("Commande compilation detect�");
			String[] listChemin = listParam[0].split(",");
			for(int i = 0; i<listChemin.length;i++)
			{
				traiterCompilation(listChemin[i]);
			}
			
			break;
		}
		case chargement : {
			System.out.println("Commande chargement detect�");
			traiterChargement(listParam[0]);
			break;
		}
		case creation : {
			System.out.println("Commande creation detect�");
			traiterCreation(Class.forName(listParam[0]), listParam[1]);
			break;
		}
		case lecture : {
			System.out.println("Commande lecture detect�");
			traiterLecture(ident.get(listParam[0]), listParam[1]);
			break;
		}
		case ecriture : {
			System.out.println("Commande ecriture detect�");
			traiterEcriture(ident.get(listParam[0]), listParam[2], listParam[3]);
			break;
		}
		case fonction : {
			System.out.println("Commande fonction detect�");
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
	 * traiterLecture : traite la lecture d�un attribut. Renvoies le r�sultat par le 
	 * socket
	 * @throws IOException 
	 */
	public void traiterLecture(Object pointeurObjet, String attribut) throws IOException 
	{
		outToClient.writeObject(new String("Hello from serveur"));
		System.out.println("R�ponse envoy�");
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
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void traiterCreation(Class classeDeLobjet, String identificateur) throws InstantiationException, IllegalAccessException 
	{
		Object tempo = classeDeLobjet.newInstance();
		ident.put(identificateur, tempo);
		
		
	}

	/**
	 * traiterChargement : traite le chargement d�une classe. Confirmes au client que la cr�ation
	 * s�est faite correctement.
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
	 * traiterCompilation : traite la compilation d�un fichier source java. Confirme au client
	 * que la compilation s�est faite correctement. Le fichier source est donn� par son chemin
	 * relatif par rapport au chemin des fichiers sources.
	 */
	public void traiterCompilation(String cheminRelatifFichierSource) 
	{
		JavaCompiler compil = ToolProvider.getSystemJavaCompiler();
		System.out.println(compil);
		compil.run(null, null, null, cheminRelatifFichierSource);
		
	}

	/**
	 * traiterAppel : traite l�appel d�une m�thode, en prenant comme argument l�objet
	 * sur lequel on effectue l�appel, le nom de la fonction � appeler, un tableau de nom de 
	 * types des arguments, et un tableau d�arguments pour la fonction. Le r�sultat de la 
	 * fonction est renvoy� par le serveur au client (ou le message que tout s�est bien 
	 * pass�)
	 * @throws IOException 
	 **/
	public void traiterAppel(Object pointeurObjet, String nomFonction, String[] types,Object[] valeurs) throws IOException 
	{
		outToClient.writeObject(new String("Hello from serveur"));
		System.out.println("R�ponse envoy�");
	}


}
