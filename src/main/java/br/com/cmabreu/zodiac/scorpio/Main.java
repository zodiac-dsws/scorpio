package br.com.cmabreu.zodiac.scorpio;

import java.io.File;

import br.com.cmabreu.zodiac.federation.federates.ScorpioFederate;


public class Main {

	public static String getUserFolder(final File folder) {
		String result = "";
	    for (final File fileEntry : folder.listFiles()) {
	    	 result = fileEntry.getName();
	    }
	    return result;
	}	
	
	public static void main( String[] args ) {
		
		Logger.getInstance().enable();
		
		try {
			
			ScorpioFederate tf = ScorpioFederate.getInstance(); 
			tf.startFederate();
			
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		
	}

}
