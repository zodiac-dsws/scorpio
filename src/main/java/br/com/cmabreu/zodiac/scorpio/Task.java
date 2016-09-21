package br.com.cmabreu.zodiac.scorpio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
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

	
	private void dump() {
		try {
			String fileName = "dump/" + owner.getSerial() + "/";
			File fil = new File( "dump" );
			fil.mkdirs();
			
			FileWriter fw = new FileWriter(fileName, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter writer = new PrintWriter(bw);
		    
			writer.println("--------------------------------------------------------");
			writer.println("Start Time         : " + activation.getStartTime() );
			writer.println("Workflow           : " + activation.getWorkflow() );
			writer.println("Experiment         : " + activation.getExperiment() );
			writer.println("Fragment           : " + activation.getFragment() );
			writer.println("Activity           : " + activation.getActivitySerial() );
			writer.println("Instance           : " + activation.getInstanceSerial() );
			writer.println("Command            : " + activation.getCommand() );
			writer.println("Executor           : " + activation.getExecutor() );
			writer.println("Executor Type      : " + activation.getExecutorType() );
			writer.println("Target Table       : " + activation.getTargetTable() );
			writer.println("--------------------------------------------------------");
			writer.println( activation.getXmlOriginalData() );
			writer.println("--------------------------------------------------------");
			writer.println("");
			writer.println("");
			writer.println("");
			writer.println("");
		    
			writer.close();
		    
		} catch ( Exception e ) {
			System.out.println("Cannot Dump Core " + owner.getSerial() );
		}
		
	}
	
	/**
	 * BLOCKING
	 * Will execute a external program (wrapper)
	 * WIll block until task is finished
	 * 
	 */
	public void executeCommand( String command ) throws Exception {
		dump();
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
		dump();
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