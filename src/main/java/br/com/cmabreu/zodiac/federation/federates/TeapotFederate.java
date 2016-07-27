package br.com.cmabreu.zodiac.federation.federates;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import br.com.cmabreu.zodiac.federation.Environment;
import br.com.cmabreu.zodiac.federation.RTIAmbassadorProvider;
import br.com.cmabreu.zodiac.federation.classes.CoreClass;
import br.com.cmabreu.zodiac.federation.classes.FinishedInstanceInteractionClass;
import br.com.cmabreu.zodiac.federation.classes.RequestTaskInteractionClass;
import br.com.cmabreu.zodiac.federation.classes.RunInstanceInteractionClass;
import br.com.cmabreu.zodiac.federation.classes.RunInstanceInteractionClass.InstanceCommandPack;
import br.com.cmabreu.zodiac.federation.classes.SagitariiClass;
import br.com.cmabreu.zodiac.federation.classes.TeapotClass;
import br.com.cmabreu.zodiac.federation.objects.TeapotObject;
import br.com.cmabreu.zodiac.scorpio.Logger;
import br.com.cmabreu.zodiac.scorpio.SystemProperties;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;

public class TeapotFederate {
	private String rootPath;
	private static TeapotFederate instance;
	private SagitariiClass sagitariiClass;
	private TeapotClass teapotClass;
	private CoreClass coreClass;
	private RequestTaskInteractionClass requestTaskInteractionClass;
	private RunInstanceInteractionClass runInstanceInteractionClass;
	private FinishedInstanceInteractionClass finishedInstanceInteractionClass;
	
	public FinishedInstanceInteractionClass getFinishedInstanceInteractionClass() {
		return finishedInstanceInteractionClass;
	}
	
	public RequestTaskInteractionClass getRequestTaskInteractionClass() {
		return requestTaskInteractionClass;
	}
	
	public RunInstanceInteractionClass getRunInstanceInteractionClass() {
		return runInstanceInteractionClass;
	}

	public static TeapotFederate getInstance() throws Exception {
		if ( instance == null ) {
			instance = new TeapotFederate();
		}
		return instance;
	}
	
	public void requestTask( String coreSerial ) throws Exception {
		requestTaskInteractionClass.send( coreSerial, "teapot" );
	}
	
	public void notifyFederationInsaceFinished( String coreSerial, String instanceSerial ) throws Exception {
		System.out.println("Finished instance " + instanceSerial + " by core " + coreSerial );
		finishedInstanceInteractionClass.send(coreSerial, instanceSerial);
	}
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	
	
	private void warn( String s ) {
		Logger.getInstance().warn(this.getClass().getName(), s );
	}	

	public void finishFederationExecution() throws Exception {
		debug( "Will try to finish Federation execution" );
		//rtiamb.resignFederationExecution( ResignAction.DELETE_OBJECTS );
		
		RTIambassador rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();

		try	{
			rtiamb.destroyFederationExecution( "Sagitarii" );
			debug( "Destroyed Federation" );
		} catch( FederationExecutionDoesNotExist dne ) {
			debug( "No need to destroy federation, it doesn't exist" );
		} catch( FederatesCurrentlyJoined fcj ){
			debug( "Didn't destroy federation, federates still joined" );
		}		
	}

	
	public TeapotFederate( ) throws Exception {
		debug("Starting Sagitarii Federation");
		this.rootPath = SystemProperties.getInstance().getTeapotRootFolder();
		
		try {

			Map<String, String> newenv = new HashMap<String, String>();
			newenv.put("RTI_HOME", "");
			//newenv.put("RTI_RID_FILE", rootPath + "/rti.RID" );
			Environment.setEnv( newenv );
			
			RTIambassador rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
			
			try	{
				URL[] modules = new URL[]{
					(new File(rootPath + "/foms/HLAstandardMIM.xml")).toURI().toURL()
				};
				
				rtiamb.createFederationExecution("Sagitarii", modules );
			} catch( FederationExecutionAlreadyExists exists ) {
				debug("Sagitarii Federation already exists. Bypassing...");
			}
			
			join();
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
	}
	
	public void processInstance( ParameterHandleValueMap theParameters ) throws Exception {
		InstanceCommandPack icp = runInstanceInteractionClass.getInstanceHexDef( theParameters );
		if ( icp != null  ) {
			coreClass.processInstance(icp.targetCore, icp.instanceHex);
		}
	}
	
	public void sagitariiIsDown() {
		// What need I do?
	}
	
	public void sagitariiIsUp() {
		// What need I do?
	}

	public void startFederate() throws Exception {
		debug("starting server...");
		if ( sagitariiClass == null ) {

			// Publish my attributes
			// DO NOT SUBSCRIBE OR WILL RECEIVE OTHER TEAPOTS IN A INFINITE LOOP
			teapotClass = new TeapotClass();
			teapotClass.publish();
			// Create one node
			TeapotObject to = teapotClass.createNew();

			// Subscribe to server updates
			// and will know when server is down ;-)
			sagitariiClass = new SagitariiClass();
			sagitariiClass.subscribe();
			
			// Publish Cores attributes 
			// DO NOT SUBSCRIBE OR WILL RECEIVE OTHER CORES
			coreClass = new CoreClass();
			coreClass.publish();

			// Will register machine cores into RTI
			for (int x=0; x < to.getAvailableProcessors(); x++  ) {
				coreClass.createNew( to.getMacAddress() );		
			}

			// Interactions
			// Publish to request tasks from Sagitarii
			requestTaskInteractionClass = new RequestTaskInteractionClass();
			requestTaskInteractionClass.publish();
			// Subscribe to listen for incomming tasks
			runInstanceInteractionClass = new RunInstanceInteractionClass();
			runInstanceInteractionClass.subscribe();
			
			// Publish to tell everybody I finished an instance
			finishedInstanceInteractionClass = new FinishedInstanceInteractionClass();
			finishedInstanceInteractionClass.publish();
			
			
			debug("done.");
			
			while ( System.in.available() == 0 ) {

				teapotClass.updateAttributeValues();
				coreClass.requestInstances();
				
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					//
				}				
			}			

			
		} else {
			warn("server is already running an instance");
		}
	}
	
	public SagitariiClass getSagitariiClass() {
		return sagitariiClass;
	}
	
	public TeapotClass getTeapotClass() {
		return teapotClass;
	}
	
	public CoreClass getCoreClass() {
		return coreClass;
	}
	
	private void join() throws Exception {
		RTIambassador rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();

		debug("joing Federation Execution using FOM SagitariiFederation.xml...");
		URL[] joinModules = new URL[]{
		    (new File(rootPath +  "/foms/SagitariiFederation.xml")).toURI().toURL()
		};
		
		String mac = SystemProperties.getInstance().getMacAddress();
		
		rtiamb.joinFederationExecution( "Teapot Node " + mac, "SagitariiType", "Sagitarii", joinModules );           
	}
	
}
