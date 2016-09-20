package br.com.cmabreu.zodiac.scorpio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.cmabreu.zodiac.federation.objects.CoreObject;
import br.com.cmabreu.zodiac.scorpio.misc.Activation;
import br.com.cmabreu.zodiac.scorpio.services.RelationService;
import br.com.cmabreu.zodiac.scorpio.types.ExecutorType;



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
	public void executeCommand( String command ) throws Exception {
		File fil = new File ( "d:/echo/" + owner.getSerial() + ".txt" );
		fil.createNewFile();
		FileWriter fw = new FileWriter(fil.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for ( int x = 0; x < 20000; x++ ) {
			for ( int y = 0; y < 200; y++ ) {
				bw.write( ">> " + command + " = " + y + "," + x + "\n" );
			}
		}
		bw.close();
	}

	/**
	 * BLOCKING
	 * Will execute a external program (wrapper)
	 * WIll block until task is finished
	 * 
	 */
	public void executeSqlCommand( String command ) throws Exception {
		RelationService rs = new RelationService();
		rs.executeQuery( command );
		
		File fil = new File ( "d:/echo/" + owner.getSerial() + ".txt" );
		fil.createNewFile();
		FileWriter fw = new FileWriter(fil.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write( command );
		bw.close();
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
		int exitCode = 0;
		try {
			if ( activation.getExecutorType() == ExecutorType.SELECT ) {
				executeSqlCommand( activation.getCommand() );
			} else {
				executeCommand( activation.getCommand() );
			}
		} catch ( Exception e ) {
			exitCode = 1;
		}
		owner.notifyFinishedByTask( exitCode );
	}

}