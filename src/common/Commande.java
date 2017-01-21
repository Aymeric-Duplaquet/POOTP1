package common;

import java.io.Serializable;

public class Commande implements Serializable{

	private String commande;
	
	public void setCommande(String in)
	{
		commande = in;
	}
	public String getCommande()
	{
		return commande;
	}
}
