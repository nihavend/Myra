package com.likya.myra.jef.utils;

public class PrintVantil {
	
	private static int counter = 0;
	
	static char vantil [] = {'-', '\\', '|', '/'};

	public static char getVantil() {
		
		char ch = vantil[counter++];
		
		if(counter >= vantil.length) {
			counter = 0;
		} 
		
		return ch;
	}
	
}
