package common;

import java.io.Serializable;


public class Resultat implements Serializable{


	//Serial UID
	private static final long serialVersionUID = -1767893394379159831L;

	//Etat du resultat. Problem = false => Commande a �t� �x�cut� sans probl�me
	public Boolean problem;
	
	//Resultat de la commande, si celle-ci a produit un  resultat
	public Object resultat;
	
	
	// Comment faire la distinction coté client entre un résultat qui s'est bien passé mais qui retourne null(écriture, compilation)
	// et un résultat qui s'est mal passé mais qui retourne aussi nul.
	// Coté client il faut que je check la variable problème, il ne peut pas seulement se contenter d'évaluer le string.
	
	public Resultat()
	{
		problem = true;
		resultat = null;
	}
	

	public Resultat(Object in)
	{
		problem = false;
		resultat = in;
	}
	
	public String toString()
	{
		if(resultat == null)
		{
			return "";
		}
		else
		{
			return resultat.toString();
		}
		
	}
	
	public Boolean getProblem() {
		return problem;
	}

}
