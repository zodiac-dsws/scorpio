package br.com.cmabreu.zodiac.scorpio;

import java.io.File;

import br.com.cmabreu.zodiac.federation.federates.ScorpioFederate;
import br.com.cmabreu.zodiac.scorpio.config.Configurator;
import br.com.cmabreu.zodiac.scorpio.infra.ConnFactory;

public class Main {

	public static String getUserFolder(final File folder) {
		String result = "";
	    for (final File fileEntry : folder.listFiles()) {
	    	 result = fileEntry.getName();
	    }
	    return result;
	}
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	

	private void error( String s ) {
		Logger.getInstance().error(this.getClass().getName(), s );
	}	
	
    public static void main( String[] args ) {
    	System.out.println("Starting Scorpio...");
    	new Main().initialize();
    }	
	
	
	public void initialize() {
		
		try {

			Logger.getInstance().enable();
			Logger.getInstance().canOutputToFile( true );
			
			Configurator config = Configurator.getInstance("config.xml");
			config.loadMainConfig();
			
			String user = config.getUserName();
			String passwd = config.getPassword();
			String database = config.getDatabaseName();

			debug("Credentials: " + user + " | " + database);
			
    		ConnFactory.setCredentials(user, passwd, database);			
			
			ScorpioFederate tf = ScorpioFederate.getInstance(); 
			tf.startFederate();
			
		} catch ( Exception e ) {
			error( "Error starting Scorpio: " + e.getMessage() );
			e.printStackTrace();
		}

		
	}

}
