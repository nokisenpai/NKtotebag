package be.noki_senpai.NKtotebag.utils;

public class CheckType 
{
	public static boolean isNumber(String str)  
	{  
		return str.matches("[-+]?\\d+(\\.\\d+)?");
	}
	
	public static boolean isAlphaNumeric(String str)  
	{  
		return str.matches("[a-zA-Z0-9\\-_]+");
	}
}
