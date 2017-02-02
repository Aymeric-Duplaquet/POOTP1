package common;

public class Resultat {
	public boolean problem;
	public Object resultat;
	
	Resultat()
	{
		problem = true;
		resultat = null;
	}
	
	Resultat(Object in)
	{
		problem = false;
		resultat = in;
	}

}
