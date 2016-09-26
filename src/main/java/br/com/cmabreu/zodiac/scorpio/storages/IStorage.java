package br.com.cmabreu.zodiac.scorpio.storages;

import br.com.cmabreu.zodiac.scorpio.misc.Activation;
import br.com.cmabreu.zodiac.scorpio.misc.FileUnity;

public interface IStorage {
	boolean copyToLocalFS(Activation act, FileUnity file, String target);
	void copyToRemoteFS(String localFile, String targetFile);
	void copyToNextActivity(String localFile, String targetFile);
}
