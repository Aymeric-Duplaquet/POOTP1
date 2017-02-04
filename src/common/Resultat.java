package common;

import java.io.Serializable;


public class Resultat implements Serializable{


	//Serial UID
	private static final long serialVersionUID = -1767893394379159831L;

	//Etat du résultat. Problem = false => Commande a été éxécuté sans problème
	public Boolean problem;
	
	//Résultat de la commande, si celle-ci a produit un résultat
	public Object resultat;
	
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

}
