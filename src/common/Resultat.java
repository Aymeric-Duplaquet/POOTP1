package common;

import java.io.Serializable;


public class Resultat implements Serializable{


	//Serial UID
	private static final long serialVersionUID = -1767893394379159831L;

	//Etat du r�sultat. Problem = false => Commande a �t� �x�cut� sans probl�me
	public Boolean problem;
	
	//R�sultat de la commande, si celle-ci a produit un r�sultat
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
