package br.com.cmabreu.zodiac.scorpio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.cmabreu.zodiac.federation.objects.CoreObject;



public class Task implements Runnable {
	private List<String> sourceData;
	private List<String> console;
	private int exitCode;
	private Date realStartTime;
	private Date realFinishTime;
	private int PID;
	private CoreObject owner;
	private Activation activation;
	
	public int getPID() {
		return PID;
	}

	public Date getRealFinishTime() {
		return realFinishTime;
	}

	public Date getRealStartTime() {
		return realStartTime;
	}

	public void setRealFinishTime(Date realFinishTime) {
		this.realFinishTime = realFinishTime;
	}

	public void setRealStartTime(Date realStartTime) {
		this.realStartTime = realStartTime;
	}

	public List<String> getSourceData() {
		return sourceData;
	}

	public List<String> getConsole() {
		return console;
	}

	public void setSourceData(List<String> sourceData) {
		this.sourceData = sourceData;
	}


	public Task( CoreObject owner ) {
		this.console = new ArrayList<String>();
		this.owner = owner;
	}

	/**
	 * BLOCKING
	 * Will execute a external program (wrapper)
	 * WIll block until task is finished
	 * 
	 */
	public void executeCommand( String command ) {
		try {
			File fil = new File ( "f:/echo/" + owner.getSerial() + ".txt" );
			fil.createNewFile();
			FileWriter fw = new FileWriter(fil.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for ( int x = 0; x < 20000; x++ ) {
				for ( int y = 0; y < 400; y++ ) {
					bw.write( ">> " + y + "," + x + "\n" );
				}
			}
			bw.close();
		} catch ( Exception e ) {
			
		}
	}

	public int getExitCode() {
		return this.exitCode;
	}

	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}
	
	public void setActivation( Activation act ) {
		this.activation = act;
	}
	
	public Activation getActivation() {
		return activation;
	}

	@Override
	public void run() {
		executeCommand( "" );
		owner.notifyFinishedByTask();
	}

}