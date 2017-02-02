package common;

import java.io.Serializable;

public class Commande implements Serializable{
	
	// La Commande demandée est stockée dans un String.
	private String commande;
	// Chaque Commande se voit attribuer un type selon une énumération.
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
