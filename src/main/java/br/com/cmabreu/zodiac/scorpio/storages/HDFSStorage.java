package br.com.cmabreu.zodiac.scorpio.storages;


import br.com.cmabreu.zodiac.scorpio.Logger;
import br.com.cmabreu.zodiac.scorpio.misc.Activation;
import br.com.cmabreu.zodiac.scorpio.misc.FileUnity;

public class HDFSStorage implements IStorage {

	@Override
	public boolean copyToLocalFS(Activation act, FileUnity file, String target) {
		return true;
	}

	@Override
	public void copyToRemoteFS(String localFile, String targetFile) {
		debug(" >>> Copy file " + localFile + " to " + targetFile );
		
	}

	@Override
	public void copyToNextActivity(String localFile, String targetFile) {
		
	}
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	
	
	
}
