package serveur;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
		/*
		serveur.traiterCompilation("./src/ca/uqac/registraire/Cours.java");
		serveur.traiterChargement("ca.uqac.registraire.Cours");
		serveur.traiterCreation(Class.forName("ca.uqac.registraire.Cours"), "Hello");
		
		System.out.println(serveur.ident.get("Hello").toString());
		*/

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
		System.out.println("Nouvelle commande : ");
		System.out.println("Serveur : "+ inCmd.getCommande());
		try {
			traiteCommande(inCmd);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * prend uneCommande dument formattï¿½e, et la traite. Dï¿½pendant du type de commande, 
	 * elle appelle la mï¿½thode spï¿½cialisï¿½e
	 */
	public void traiteCommande(Commande uneCommande) throws Exception 
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
			try {
				traiterLecture(ident.get(listParam[0]), listParam[1]);
			} catch (Exception e) {
				outToClient.writeObject(null);
				System.out.println("Réponse envoyé");
			}
			break;
		}
		case ecriture : {
			System.out.println("Commande ecriture detectï¿½");
			try {
				traiterEcriture(ident.get(listParam[0]), listParam[1], listParam[2]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		}
		case fonction : {
			System.out.println("Commande fonction detectï¿½");
			String[] tabListParam;
			if(listParam.length>=3)
			{
				tabListParam = listParam[2].split(",");
			}
			else
			{
				tabListParam = new String[0];
			}
			
			String[] tabTypeParam = new String[tabListParam.length];
			Object[] tabParam = new Object[tabListParam.length];
			
			for(int i = 0;i<tabListParam.length;i++)
			{
				String[] temp = tabListParam[i].split(":");
				tabTypeParam[i] = temp[0];
				
				//TODO : cast le string en fonction du type précisé 
				if(temp[1].startsWith("ID("))
				{
					System.out.println("Param ID");
					String identIn = temp[1].substring(3, temp[1].length()-1);
					tabParam[i]=ident.get(identIn);
					if(tabTypeParam[i].compareTo(tabParam[i].getClass().getName())!=0)
					{
						System.out.println("Not good");
						outToClient.writeObject(null);
						return;
					}
				}
				else
				{
					tabParam[i] = stringToObject(temp[0], temp[1]);
				}
				
			}
			System.out.println("Appel De la fonction");
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
	 * @throws Exception 
	 */
	public void traiterLecture(Object pointeurObjet, String attribut) throws Exception 
	{
		Class classObject= pointeurObjet.getClass();
		
		try {
			Field tempoField = classObject.getField(attribut);
			outToClient.writeObject(tempoField.get(pointeurObjet));
			System.out.println("Réponse envoyé");
			
		} 
		catch (NoSuchFieldException e) {
			String temp = attribut.substring(0, 1).toUpperCase() + attribut.substring(1, attribut.length());
			String nomGetteur = "get" + temp;
			System.out.println(nomGetteur);
			
			Method m = classObject.getMethod(nomGetteur, null);
			Object resultat = m.invoke(pointeurObjet, null);
			outToClient.writeObject(resultat);
			System.out.println("Réponse envoyé");

		}
	}

	/**
	 * traiterEcriture : traite lï¿½ï¿½criture dï¿½un attribut. Confirmes au client que lï¿½ï¿½criture
	 * sï¿½est faite correctement.
	 * 
	 */
	public void traiterEcriture(Object pointeurObjet, String attribut, String valeur) throws Exception
	{
		Class classObject= pointeurObjet.getClass();
		
		try {
			Field tempoField = classObject.getField(attribut);
			tempoField.set(pointeurObjet, valeur);
			
		} 
		catch (NoSuchFieldException e) {
			String temp = attribut.substring(0, 1).toUpperCase() + attribut.substring(1, attribut.length());
			String nomSetteur = "set" + temp;
			System.out.println(nomSetteur);
			
			Method m = null;
			Method[] tabM = classObject.getMethods();
			for(int i=0;i<tabM.length && m == null;i++)
			{
				if(tabM[i].getName().compareTo(nomSetteur) == 0)
				{
					m=tabM[i];
				}
			}
			if(m!=null)
			{
				Class[] listParam = m.getParameterTypes();
				m.invoke(pointeurObjet, stringToObject(listParam[0].getName(),valeur));
			}
			else
			{
				throw new NoSuchMethodException();
			}
		}
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
	public void traiterAppel(Object pointeurObjet, String nomFonction, String[] types,Object[] valeurs) throws IOException , Exception
	{
		Class[] listType= new Class[types.length];
		for(int i = 0; i<listType.length;i++)
		{
			listType[i] = Class.forName(types[i]);
		}
		
		Method m = pointeurObjet.getClass().getMethod(nomFonction, listType);
		Object temp = m.invoke(pointeurObjet, valeurs);
		outToClient.writeObject(temp);
		System.out.println("Réponse envoyé");
		
	}

	private Object stringToObject(String typeVoulu,String valeur)
	{
		System.out.println(typeVoulu);
		if(typeVoulu.compareTo("java.lang.String")==0)
		{
			return valeur;
		}
		else if(typeVoulu.compareTo("java.lang.Float")==0 || typeVoulu.compareTo("float")==0 )
		{
			return Float.parseFloat(valeur);
		}
		return null;
	}
}
