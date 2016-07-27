package br.com.cmabreu.zodiac.scorpio;

import java.io.File;

import br.com.cmabreu.zodiac.federation.federates.TeapotFederate;


public class Main {

	public static String getUserFolder(final File folder) {
		String result = "";
	    for (final File fileEntry : folder.listFiles()) {
	    	 result = fileEntry.getName();
	    }
	    return result;
	}	
	
	public static void main( String[] args ) {
		
		try {
			
			TeapotFederate tf = TeapotFederate.getInstance(); 
			tf.startFederate();
			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
	}

}
