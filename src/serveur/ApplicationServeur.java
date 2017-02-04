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



public class ApplicationServeur {

	private ServerSocket serverSocket;	
	//Liste des objets crees par le client sur le serveur par la commande Creation.
	private Hashtable<String, Object> ident;
	//Liste des classes chargees par le client sur le serveur par la commande Chargement.
	private Hashtable<String, Class<? extends Object>> classTable;


	//Fonction main
	public static void main(String[] args) throws IOException
	{
	
		
		//Creation du serveur
		int port = Integer.parseInt(args[0]);
		ApplicationServeur serveur = new ApplicationServeur(port);



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
	 * prend le numero de port, cree un SocketServer sur le port
	 * @throws IOException 
	 */
	public ApplicationServeur (int port) throws IOException 
	{
		serverSocket = new ServerSocket(port);
		ident = new Hashtable<String,Object>();
		classTable = new Hashtable<String,Class<? extends Object>>();
	}

	/**
	 * Se met en attente de connexion des clients. Suite aux connexions, elle lit
	 * ce qui est envoye  a travers la Socket, recrute un objet Commande envoye par
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
		// Pour ecrire des informations a destination du client.
		ObjectOutputStream outToClient  = new ObjectOutputStream(clientSocket.getOutputStream());
		// Pour lire les informations en provenance du client.
		ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());	
		
		//Objet commande entrant
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
			Resultat temp = traiteCommande(inCmd);
			outToClient.writeObject(temp);
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
		
		//Separation des parametres
		String[] listParam = strCmd.split("#");

		//Switch sur le type de la commande
		switch (uneCommande.getType()) {
		case Compilation : {
			System.out.println("Commande compilation detect� : " + strCmd);
			//Separation des differents fichiers compiles
			String[] listChemin = listParam[0].split(",");
			//Compilation des differents fichiers
			for(int i = 0; i<listChemin.length;i++)
			{
				traiterCompilation(listChemin[i]);
			}
			//Si pas d'exception, on retourne un resultat sans erreur.
			return new Resultat(null);
		}
		case Chargement : {
			System.out.println("Commande chargement detect�: " + strCmd);
			//Chargement de la classe
			traiterChargement(listParam[0]);
			return new Resultat(null);
		}
		case Creation : {
			System.out.println("Commande creation detect�: " + strCmd);
			//Creation d'une instance d'une classe
			traiterCreation(forNamePrimitive(listParam[0]), listParam[1]);
			return new Resultat(null);
		}
		case Lecture : {
			System.out.println("Commande lecture detect�: " + strCmd);
			//Lecture d'un attribut d'une classe deja instancie cote serveur
			return new Resultat(traiterLecture(ident.get(listParam[0]), listParam[1]));
		}
		case Ecriture : {
			System.out.println("Commande ecriture detect�: " + strCmd);
			//Ecriture d'un attribut dans une instance de classe cote serveur 
			traiterEcriture(ident.get(listParam[0]), listParam[1], listParam[2]);
			return new Resultat(null);
		}
		case Fonction : {
			System.out.println("Commande fonction detect�: " + strCmd);
			
			String[] tabListParam;
			if(listParam.length>=3) //Cas la fonction prend 1 ou plusieurs parametres 
			{
				tabListParam = listParam[2].split(",");
			}
			else // Cas la fonction ne prend pas de parametres
			{
				tabListParam = new String[0];
			}
			
			//Construction des tableaux des types et valeurs de la fonction
			String[] tabTypeParam = new String[tabListParam.length];
			Object[] tabParam = new Object[tabListParam.length];
			for(int i = 0;i<tabListParam.length;i++)
			{
				String[] temp = tabListParam[i].split(":");
				//Le type du parametre
				tabTypeParam[i] = temp[0];
				
				
				if(temp[1].startsWith("ID(")) // Cas le parametre est un objet cote serveur
				{
					//On obtient l'identifiant au milieu de ID(*)
					String identIn = temp[1].substring(3, temp[1].length()-1);
					//On recupere l'objet
					tabParam[i]=ident.get(identIn);
					
					//On test que l'objet est du bon type
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
			//On traite l'appel et on renvoi le resultat
			return new Resultat(traiterAppel(ident.get(listParam[0]), listParam[1], tabTypeParam, tabParam));
		}
		default:
			System.out.println("Commande non reconnue");
			//On envoie resultat
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
			//Si le champ est publique, on l'utilise directement
			Field tempoField = classObject.getField(attribut);
			return tempoField.get(pointeurObjet);

		} 
		catch (NoSuchFieldException e) {
			//Sinon on cherche le get du champ
			//Construction du nom de la methode
			String temp = attribut.substring(0, 1).toUpperCase() + attribut.substring(1, attribut.length());
			String nomGetteur = "get" + temp;
			
			//Recherche de la methode et appel
			Method m = classObject.getMethod(nomGetteur, (Class<?>[])null);
			Object resultat = m.invoke(pointeurObjet,(Object[]) null);
			//Retour du resultat si methode a pu etre appelle
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
			//Si le champ est publique, on �crit directement
			Field tempoField = classObject.getField(attribut);
			tempoField.set(pointeurObjet, valeur);

		} 
		catch (NoSuchFieldException e) {
			//Cas le champ n'est pas publique
			//Construction du nom du setteur
			String temp = attribut.substring(0, 1).toUpperCase() + attribut.substring(1, attribut.length());
			String nomSetteur = "set" + temp;

			
			//On obtien la liste de toutes les m�thode
			Method m = null;
			Method[] tabM = classObject.getMethods();
			for(int i=0;i<tabM.length && m == null;i++)
			{
				//On cherche la m�thode avec le nom du setteur
				if(tabM[i].getName().compareTo(nomSetteur) == 0)
				{
					m=tabM[i];
				}
			}
			if(m!=null)
			{
				//On a trouv� la m�thode
				//On trouve le type du param�tre voulu. (Et donc le type du champ � �crire, normalement)
				Class<? extends Object>[] listParam = m.getParameterTypes();
				//On invoque la m�thode avec une transcription de la valeur de string au type voulu
				m.invoke(pointeurObjet, stringToObject(listParam[0].getName(),valeur));
			}
			else
			{
				//Pas de m�thode de setteur, on envoie une exception
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
		//Instantiation de l'objet
		Object tempo = classeDeLobjet.newInstance();
		//On garde une trace de l'objet � partir de son identificatieur
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
		//On r�cup�re le classLoader du syst�me
		ClassLoader classLoad = ClassLoader.getSystemClassLoader();
		//On charge la classe et on garde une trace de la classe charg�
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
		//On compile la classe
		JavaCompiler compil = ToolProvider.getSystemJavaCompiler();
		compil.run(null, null, null, cheminRelatifFichierSource);

	}

	/**
	 * traiterAppel : traite l�appel d�une m�thode, en prenant comme argument l�objet
	 * sur lequel on effectue l�appel, le nom de la fonction � appeler, un tableau de nom de 
	 * types des arguments, et un tableau d�arguments pour la fonction. Le r�sultat de la 
	 * fonction est renvoy� par le serveur au client (ou le message que tout s�est bien 
	 * pass�)
	 * @throws Exception 
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
		else if(typeVoulu.compareTo("java.lang.Short")==0 || typeVoulu.compareTo("short")==0)
		{
			return Short.parseShort(valeur);
		}
		else if(typeVoulu.compareTo("java.lang.Integer")==0 || typeVoulu.compareTo("int")==0)
		{
			return Integer.parseInt(valeur);
		} 
		else if(typeVoulu.compareTo("java.lang.Long")==0 || typeVoulu.compareTo("long")==0)
		{
			return Long.parseLong(valeur);
		}
		else if(typeVoulu.compareTo("java.lang.Double")==0 || typeVoulu.compareTo("double")==0)
		{
			return Double.parseDouble(valeur);
		}
		return valeur;
	}
	
	//Un forname incluant les types primitifs.
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
			else if(className.compareTo("byte")==0)
			{
				return byte.class;
			}
			else if(className.compareTo("short")==0)
			{
				return short.class;
			}
			else if(className.compareTo("int")==0)
			{
				return int.class;
			}
			else if(className.compareTo("long")==0)
			{
				return long.class;
			}
			else if(className.compareTo("double")==0)
			{
				return double.class;
			}
			else if(className.compareTo("boolean")==0)
			{
				return boolean.class;
			}
			else if(className.compareTo("char")==0)
			{
				return char.class;
			}
			throw e1;
		}
		
	}
}
