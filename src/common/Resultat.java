package common;

import java.io.Serializable;


public class Resultat implements Serializable{



	//Serial UID
	private static final long serialVersionUID = 2895296755313579268L;

	//Etat du resultat. Problem = false => Commande effectuee sans probleme
	public Boolean problem;
	
	//Resultat de la commande, si celle-ci a produit un  resultat
	public Object resultat;
	
	
	// Comment faire la distinction cote client entre un resultat qui s'est bien passe mais qui retourne null(ecriture, compilation)
	// et un resultat qui s'est mal passe mais qui retourne aussi nul.
	// Cote client il faut que je check la variable probleme, il ne peut pas seulement se contenter d'evaluer le string.
	
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
