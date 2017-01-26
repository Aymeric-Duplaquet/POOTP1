package common;

import java.io.Serializable;

public class Commande implements Serializable{

	private String commande;
	private TypeCommande type;
	
	public TypeCommande getType() {
		return type;
	}
	
	public void setType(TypeCommande type) {
		this.type = type;
	}
	
	public void setCommande(String in)
	{
		commande = in;
	}
	public String getCommande()
	{
		return commande;
	}
}
