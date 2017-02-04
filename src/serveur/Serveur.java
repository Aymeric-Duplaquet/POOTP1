package serveur;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Hashtable;
import java.io.*;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import common.Commande;
import common.Resultat;



public class Serveur {

	private ServerSocket serverSocket;
	
	private Hashtable<String, Object> ident;
	private Hashtable<String, Class<? extends Object>> classTable;


	//Fonction main
	public static void main(String[] args) throws IOException
	{
		//Creation du serveur
		int port = Integer.parseInt(args[0]);
		Serveur serveur = new Serveur(port);



		//Boucle du serveur
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
	}

	/**
	 * prend le num�ro de port, cr�e un SocketServer sur le port
	 * @throws IOException 
	 */
	public Serveur (int port) throws IOException 
	{
		serverSocket = new ServerSocket(port);
		ident = new Hashtable<String,Object>();
		classTable = new Hashtable<String,Class<? extends Object>>();
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
	public void aVosOrdres() throws IOException
	{
		//Ouverture du socket
		Socket clientSocket = serverSocket.accept();
		// Pour écrire des informations à destination du client.
		ObjectOutputStream outToClient  = new ObjectOutputStream(clientSocket.getOutputStream());
		// Pour lire les informations en provenance du client.
		ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());	
		
		//Object commande entrant
		Object inputObject;
		Commande inCmd;
		//Attente d'une commande
		while(true)
		{
			try
			{
				inputObject = inFromClient.readObject();
				if(inputObject!=null)
				{
					inCmd = (Commande) inputObject;
					break;
				}
			}
			catch(ClassNotFoundException e)
			{
				e.printStackTrace();
			}

		}
		
		//Traitement de la commande
		try {
			Object temp = traiteCommande(inCmd);
			outToClient.writeObject(new Resultat(temp));
		} catch (Exception e) {
			e.printStackTrace();
			outToClient.writeObject(new Resultat());
		}

	}

	/**
	 * prend uneCommande dument formatt�e, et la traite. D�pendant du type de commande, 
	 * elle appelle la m�thode sp�cialis�e
	 */
	public Resultat traiteCommande(Commande uneCommande) throws Exception 
	{

		String strCmd = uneCommande.getCommande();
		String[] listParam = strCmd.split("#");

		switch (uneCommande.getType()) {
		case Compilation : {
			System.out.println("Commande compilation detect� : " + strCmd);
			String[] listChemin = listParam[0].split(",");
			for(int i = 0; i<listChemin.length;i++)
			{
				traiterCompilation(listChemin[i]);
			}
			return new Resultat(null);
		}
		case Chargement : {
			System.out.println("Commande chargement detect�: " + strCmd);
			traiterChargement(listParam[0]);
			return new Resultat(null);
		}
		case Creation : {
			System.out.println("Commande creation detect�: " + strCmd);
			traiterCreation(Class.forName(listParam[0]), listParam[1]);
			return new Resultat(null);
		}
		case Lecture : {
			System.out.println("Commande lecture detect�: " + strCmd);
			return new Resultat(traiterLecture(ident.get(listParam[0]), listParam[1]));
		}
		case Ecriture : {
			System.out.println("Commande ecriture detect�: " + strCmd);
			traiterEcriture(ident.get(listParam[0]), listParam[1], listParam[2]);
			return new Resultat(null);
		}
		case Fonction : {
			System.out.println("Commande fonction detect�: " + strCmd);
			
			String[] tabListParam;
			if(listParam.length>=3) //Cas la fonction prend 1 ou plusieurs param�tres 
			{
				tabListParam = listParam[2].split(",");
			}
			else // Cas la fonction ne prend pas de param�tres
			{
				tabListParam = new String[0];
			}
			
			//Construction des tableaux des types et valeurs de la fonction
			String[] tabTypeParam = new String[tabListParam.length];
			Object[] tabParam = new Object[tabListParam.length];
			for(int i = 0;i<tabListParam.length;i++)
			{
				String[] temp = tabListParam[i].split(":");
				//Le type du param�tre
				tabTypeParam[i] = temp[0];
				
				
				if(temp[1].startsWith("ID(")) // Cas le param�tre est un objet cot� serveur
				{
					//On obtient l'identifiant au milieu de ID(*)
					String identIn = temp[1].substring(3, temp[1].length()-1);
					tabParam[i]=ident.get(identIn);
					
					//On test que l'identifiant est du bon type
					if(tabTypeParam[i].compareTo(tabParam[i].getClass().getName())!=0)
					{
						throw new IllegalArgumentException();
					}
				}
				else
				{
					tabParam[i] = stringToObject(temp[0], temp[1]);
				}

			}
			
			return new Resultat(traiterAppel(ident.get(listParam[0]), listParam[1], tabTypeParam, tabParam));
		}
		default:
			System.out.println("Commande non reconnue");
			return new Resultat();
		}
	}

	/**
	 * traiterLecture : traite la lecture d�un attribut. Renvoies le r�sultat par le 
	 * socket
	 * @throws Exception 
	 */
	public Object traiterLecture(Object pointeurObjet, String attribut) throws Exception 
	{
		Class<? extends Object> classObject= pointeurObjet.getClass();

		try {
			Field tempoField = classObject.getField(attribut);
			return tempoField.get(pointeurObjet);

		} 
		catch (NoSuchFieldException e) {
			String temp = attribut.substring(0, 1).toUpperCase() + attribut.substring(1, attribut.length());
			String nomGetteur = "get" + temp;

			Method m = classObject.getMethod(nomGetteur, (Class<?>[])null);
			Object resultat = m.invoke(pointeurObjet,(Object[]) null);
			return resultat;
			

		}
	}

	/**
	 * traiterEcriture : traite l��criture d�un attribut. Confirmes au client que l��criture
	 * s�est faite correctement.
	 * 
	 */
	public void traiterEcriture(Object pointeurObjet, String attribut, String valeur) throws Exception
	{
		Class<? extends Object> classObject= pointeurObjet.getClass();

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
				Class<? extends Object>[] listParam = m.getParameterTypes();
				m.invoke(pointeurObjet, stringToObject(listParam[0].getName(),valeur));
			}
			else
			{
				throw new NoSuchMethodException();
			}
		}
	}

	/**
	 * traiterCreation : traite la cr�ation d�un objet. Confirme au client que la cr�ation
	 * s�est faite correctement.
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public void traiterCreation(Class<? extends Object> classeDeLobjet, String identificateur) throws InstantiationException, IllegalAccessException 
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
		Class<? extends Object> tempoClass = classLoad.loadClass(nomQualifie);
		classTable.put(nomQualifie, tempoClass);
	}

	/**
	 * traiterCompilation : traite la compilation d�un fichier source java. Confirme au client
	 * que la compilation s�est faite correctement. Le fichier source est donn� par son chemin
	 * relatif par rapport au chemin des fichiers sources.
	 */
	public void traiterCompilation(String cheminRelatifFichierSource) 
	{
		JavaCompiler compil = ToolProvider.getSystemJavaCompiler();
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
	public Object traiterAppel(Object pointeurObjet, String nomFonction, String[] types,Object[] valeurs) throws Exception
	{
		//On obtien le tableau des types des param�tres de la m�thode
		Class<? extends Object>[] listType= new Class<?>[types.length];
		for(int i = 0; i<listType.length;i++)
		{
			listType[i] = forNamePrimitive(types[i]);
		}
		//On recherche la m�thode
		Method m = pointeurObjet.getClass().getMethod(nomFonction, listType);
		//Invocation de la m�thode
		Object ret = m.invoke(pointeurObjet, valeurs);
		return ret;

	}

	//Fonction pour parser les String en fonction du type voulu.
	//Appliqu�e aux valeurs des commandes.
	//Manque de g�n�ricit�, absence d'une m�thode g�n�rique pour parser des string en l'objet voulus.
	//Si le type voulu n'est pas reconu, on renvois la valeur en String.
	private Object stringToObject(String typeVoulu,String valeur)
	{
		if(typeVoulu.compareTo("java.lang.String")==0)
		{
			return valeur;
		}
		else if(typeVoulu.compareTo("java.lang.Float")==0 || typeVoulu.compareTo("float")==0 )
		{
			return Float.parseFloat(valeur);
		}
		return valeur;
	}
	
	private Class<? extends Object> forNamePrimitive(String className) throws ClassNotFoundException
	{
		try
		{
			return Class.forName(className);
		}
		catch(ClassNotFoundException e1)
		{
			if(className.compareTo("float")==0)
			{
				return float.class;
			}
			throw e1;
		}
		
	}
}
