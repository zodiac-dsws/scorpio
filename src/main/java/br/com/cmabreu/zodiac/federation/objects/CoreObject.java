package br.com.cmabreu.zodiac.federation.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.cmabreu.zodiac.federation.federates.ScorpioFederate;
import br.com.cmabreu.zodiac.scorpio.Activation;
import br.com.cmabreu.zodiac.scorpio.Task;
import br.com.cmabreu.zodiac.scorpio.XMLParser;
import br.com.cmabreu.zodiac.scorpio.ZipUtil;
import hla.rti1516e.ObjectInstanceHandle;

public class CoreObject {
	private ObjectInstanceHandle objectInstance;
	private boolean working;
	private String serial;
	private String experimentSerial = "*";
	private String instanceSerial = "*";
	private String fragmentSerial = "*";
	private String activitySerial = "*";
	private String executor = "*";
	private String executorType = "*";
	private String ownerNode;
	private Task currentTask;
	private Thread thread;
	private List<Activation> executionQueue;
	private boolean requesting = false;
	private boolean checked = false;
	
	public String getExecutor() {
		return executor;
	}
	
	public void setExecutor(String executor) {
		this.executor = executor;
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
		serial = UUID.randomUUID().toString().replace("-", "").substring(0,5);
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
	
	public void setWorking(boolean working) {
		this.working = working;
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
		byte[] compressedResp = ZipUtil.toByteArray( hexCompressedInstance );
		String response = ZipUtil.decompress(compressedResp);
		XMLParser parser = new XMLParser();
		List<Activation> acts = parser.parseActivations( response );
		executionQueue.addAll( acts );	
		return acts;
	}	
	
	private void executeActivation( Activation act ) {
		instanceSerial = act.getInstanceSerial();
		activitySerial = act.getActivitySerial();
		fragmentSerial = act.getFragment();
		experimentSerial = act.getExperiment();
		executorType = act.getExecutorType();
		executor = act.getExecutor();

		
		System.out.println("New instance "+ instanceSerial +" to core " + serial );
		
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
		finishThread();
	}
	
	private void runFirst( List<Activation> acts ) {
		for ( Activation act : acts ) {
			if( act.getOrder() == 0 ) {
				
				executeActivation( act );
				
				/*
				currentActivation = act;
				found = true;
				debug("execute first task in instance " + act.getInstanceSerial() );
				instanceSerial = act.getInstanceSerial();
				executionQueue.remove(act);

				String wrappersFolder = configurator.getSystemProperties().getTeapotRootFolder() + "wrappers/";
				act.setWrappersFolder(wrappersFolder);
				
				String newCommand = generateCommand( act );
				act.setCommand( newCommand );
				saveInputData( act );
				saveXmlData( act );
				runTask( act );
				break;
				*/
			}
		}		
	}
	
	public void process( String hexResp ) {
		requesting = false;
		if ( working ) {
			System.out.println("Refused instance by " + serial + ": " + hexResp.substring(0,8).toUpperCase() );
			return;
		}
		
		System.out.println("Accepted instance by " + serial + ": " + hexResp.substring(0,8).toUpperCase() );

		
		working = true;
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
			working = false;
			e.printStackTrace();
		}
	}
	
	public void notifyFinishedByTask() {
		if ( executionQueue.size() > 0 ) {
			int nextAct = currentTask.getActivation().getOrder() + 1;
			runNext( nextAct );
		} else {
			finishThread();
		}
		
	}
	
	private void finishThread() {
		String oldInstanceSerial = instanceSerial; 
		instanceSerial = "*";
		activitySerial = "*";
		fragmentSerial = "*";
		experimentSerial = "*";
		executor = "*";
		executorType = "*";
		currentTask = null;
		working = false;
		try {
			ScorpioFederate.getInstance().getCoreClass().updateWorkingDataCore( this );
			ScorpioFederate.getInstance().notifyFederationInsaceFinished( this.getSerial(), oldInstanceSerial);
			checked = true;
			requestTask();
		} catch ( Exception e ) {
			//
		}
	}
	
	private void checkRequest() {
		if ( !checked ) {
			checked = true;
		} else {
			requesting = false;
		}
	}

	public synchronized void requestTask() throws Exception {
		if ( !isWorking() ) {
			checkRequest();
			if( !requesting ) {
				requesting = true;
				checked = false;
				ScorpioFederate.getInstance().requestTask( getSerial() );
			}
		}		
	}
	
}
