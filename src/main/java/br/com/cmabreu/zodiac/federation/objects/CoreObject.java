package br.com.cmabreu.zodiac.federation.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.cmabreu.zodiac.federation.ExecutionResult;
import br.com.cmabreu.zodiac.federation.federates.ScorpioFederate;
import br.com.cmabreu.zodiac.scorpio.Logger;
import br.com.cmabreu.zodiac.scorpio.Task;
import br.com.cmabreu.zodiac.scorpio.misc.Activation;
import br.com.cmabreu.zodiac.scorpio.misc.XMLParser;
import br.com.cmabreu.zodiac.scorpio.misc.ZipUtil;
import hla.rti1516e.ObjectInstanceHandle;

public class CoreObject {
	private ObjectInstanceHandle objectInstance;
	private boolean working = false;
	private String serial;
	private String experimentSerial = "*";
	private String instanceSerial = "*";
	private String fragmentSerial = "*";
	private String activitySerial = "*";
	private String executor = "*";
	private String executorType = "*";
	private String currentInstance = "*";
	private String ownerNode;
	private Task currentTask;
	private Thread thread;
	private List<Activation> executionQueue;
	private int result;
	
	public int getResult() {
		return result;
	}
	
	public void setResult(int result) {
		this.result = result;
	}
	
	public String getExecutor() {
		return executor;
	}
	
	public void setExecutor(String executor) {
		this.executor = executor;
	}
	
	public String getCurrentInstance() {
		return currentInstance;
	}
	
	public void setCurrentInstance(String currentInstance) {
		this.currentInstance = currentInstance;
	}
	
	public String getExecutorType() {
		return executorType;
	}
	
	public void setExecutorType(String executorType) {
		this.executorType = executorType;
	}
	
	public void setOwnerNode(String ownerNode) {
		this.ownerNode = ownerNode;
	}
	
	public Task getCurrentTask() {
		return currentTask;
	}
	
	public String getOwnerNode() {
		return ownerNode;
	}
	
	public CoreObject( ObjectInstanceHandle objectInstance ) {
		serial = UUID.randomUUID().toString().replace("-", "").substring(0,6).toUpperCase();
		this.working = false;
		this.objectInstance = objectInstance;
		this.executionQueue = new ArrayList<Activation>();
	}
	
	public boolean isWorking() {
		return working;
	}

	public boolean isMe( ObjectInstanceHandle obj ) {
		return obj.equals( objectInstance );
	}
	
	public String getSerial() {
		return serial;
	}

	public ObjectInstanceHandle getHandle() {
		return objectInstance;
	}
	
	public void setSerial(String serial) {
		this.serial = serial;
	}
	
	public String getExperimentSerial() {
		return experimentSerial;
	}

	public void setExperimentSerial(String experimentSerial) {
		this.experimentSerial = experimentSerial;
	}

	public String getInstanceSerial() {
		return instanceSerial;
	}

	public void setInstanceSerial(String instanceSerial) {
		this.instanceSerial = instanceSerial;
	}

	public String getFragmentSerial() {
		return fragmentSerial;
	}

	public void setFragmentSerial(String fragmentSerial) {
		this.fragmentSerial = fragmentSerial;
	}

	public String getActivitySerial() {
		return activitySerial;
	}

	public void setActivitySerial(String activitySerial) {
		this.activitySerial = activitySerial;
	}
	
	private List<Activation> decompress( String hexCompressedInstance ) throws Exception {
		synchronized(this) {
			byte[] compressedResp = ZipUtil.toByteArray( hexCompressedInstance );
			String response = ZipUtil.decompress( compressedResp );
			XMLParser parser = new XMLParser();
			List<Activation> acts = parser.parseActivations( response );
			executionQueue.addAll( acts );
			return acts;
		}
	}	
	
	private void debug( String s ) {
		Logger.getInstance().debug( this.getClass().getName(), s );
	}

	private void error( String s ) {
		Logger.getInstance().error( this.getClass().getName(), s );
	}	
	
	private void executeActivation( Activation act ) {
		instanceSerial = act.getInstanceSerial();
		activitySerial = act.getActivitySerial();
		fragmentSerial = act.getFragment();
		experimentSerial = act.getExperiment();
		executorType = act.getExecutorType().toString();
		executor = act.getExecutor();

		debug("Accepted instance " + instanceSerial + " by " +  serial + "@" + ownerNode + " |  Experiment " + experimentSerial + " Executor " + executor + " ("+executorType+")");
		
		currentTask = new Task( this );
		currentTask.setActivation( act );
		thread = new Thread( currentTask ); 
		thread.start();

		executionQueue.remove( act );
	}
	
	private void runNext( int order ) {
		for ( Activation act : executionQueue ) {
			if( act.getOrder() == order ) {
				executeActivation( act );
				return;
			}
		}
		finishInstanceExecution();
	}
	
	private void runFirst( List<Activation> acts ) {
		for ( Activation act : acts ) {
			if( act.getOrder() == 0 ) {
				executeActivation( act );
			}
		}		
	}
	
	public void process( String hexResp ) {
		if ( working ) {
			error("Refused instance by " + serial + "@" + ownerNode  + ": Core is busy.");
			setResult( ExecutionResult.RESULT_REFUSED );
			return;
		}
		
		result = ExecutionResult.RESULT_OK;
		working = true;
		
		try {
			ScorpioFederate.getInstance().updateWorkingDataCore( this );
		} catch ( Exception e ) {
			error("Error when updating Core attributes: " + e.getMessage() );
		}
		
		List<Activation> activations;
		try {
			activations = decompress( hexResp );
			executionQueue.addAll( activations );
			if ( activations.size() > 0 ) {
				runFirst( activations );
			} else {
				working = false;
			}
		} catch (Exception e) {
			result = ExecutionResult.RESULT_ERROR;
			finishInstanceExecution();
			e.printStackTrace();
		}
	}
	
	public void notifyFinishedByTask( int exitCode ) {
		Activation act = currentTask.getActivation();
		debug(" > Instance: " + act.getInstanceSerial() + " | Order: " + act.getOrder() + " | Activity " + act.getExecutor() + "(" + act.getActivitySerial() + ") finished." );

		if ( executionQueue.size() > 0 ) {
			int nextAct = currentTask.getActivation().getOrder() + 1;
			runNext( nextAct );
		} else {
			finishInstanceExecution();
		}
		
	}
	
	private void finishInstanceExecution() {
		String oldInstanceSerial = instanceSerial;
		// instanceSerial = "*"; // Don't do it!! Need this to control "Finish Instance" at server side.
		activitySerial = "*";
		fragmentSerial = "*";
		experimentSerial = "*";
		executor = "*";
		executorType = "*";
		currentInstance = "*";
		currentTask = null;
		working = false;
		currentInstance = "*";
		
		try {
			ScorpioFederate.getInstance().getCoreClass().updateWorkingDataCore( this );
		} catch ( Exception e ) {
			error( "Error finishing Instance " + oldInstanceSerial + ": " + e.getMessage() );
		}
	}
	
}
