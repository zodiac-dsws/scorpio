package br.com.cmabreu.zodiac.federation.federates;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import br.com.cmabreu.zodiac.federation.Environment;
import br.com.cmabreu.zodiac.federation.RTIAmbassadorProvider;
import br.com.cmabreu.zodiac.federation.classes.CoreClass;
import br.com.cmabreu.zodiac.federation.classes.ScorpioClass;
import br.com.cmabreu.zodiac.federation.objects.ScorpioObject;
import br.com.cmabreu.zodiac.scorpio.Logger;
import br.com.cmabreu.zodiac.scorpio.SystemProperties;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;

public class ScorpioFederate {
	private String rootPath;
	private static ScorpioFederate instance;
	private ScorpioClass scorpioClass;
	private CoreClass coreClass;

	public static ScorpioFederate getInstance() throws Exception {
		if ( instance == null ) {
			instance = new ScorpioFederate();
		}
		return instance;
	}
	
	private void debug( String s ) {
		Logger.getInstance().debug( this.getClass().getName(), s );
	}	
	
	private void error( String s ) {
		Logger.getInstance().error( this.getClass().getName(), s );
	}	

	public void finishFederationExecution() throws Exception {
		debug( "Will try to finish Federation execution" );
		
		//rtiamb.resignFederationExecution( ResignAction.DELETE_OBJECTS );
		RTIambassador rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();

		try	{
			rtiamb.destroyFederationExecution( "Zodiac" );
			debug( "Destroyed Federation" );
		} catch( FederationExecutionDoesNotExist dne ) {
			debug( "No need to destroy federation, it doesn't exist" );
		} catch( FederatesCurrentlyJoined fcj ){
			debug( "Didn't destroy federation, federates still joined" );
		}		
	}

	
	public ScorpioFederate( ) throws Exception {
		debug("Starting The Zodiac Federation");
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
				
				rtiamb.createFederationExecution("Zodiac", modules );
			} catch( FederationExecutionAlreadyExists exists ) {
				debug("Zodiac Federation already exists. Bypassing...");
			}
			
			join();
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
	}
	
	public void processInstance( ParameterHandleValueMap theParameters ) throws Exception {
		// 
	}
	

	public void startFederate() throws Exception {
		
		debug("starting Scorpio...");

		scorpioClass = new ScorpioClass();
		scorpioClass.publish();
		ScorpioObject to = scorpioClass.createNew();

		// Publish Cores attributes 
		// DO NOT SUBSCRIBE OR WILL RECEIVE OTHER CORES. We don't care.
		coreClass = new CoreClass();
		coreClass.publish();

		// Will register this machine cores into RTI
		for (int x=0; x < to.getAvailableProcessors(); x++  ) {
			coreClass.createNew( to.getMacAddress() );		
		}

		debug("done.");
		
		while ( System.in.available() == 0 ) {
			scorpioClass.updateAttributeValues();
			try {
				Thread.sleep(5000);
			} catch (Exception e) {
				//
			}				
		}			

	}
	
	public ScorpioClass getTeapotClass() {
		return scorpioClass;
	}
	
	public CoreClass getCoreClass() {
		return coreClass;
	}
	
	private void join() throws Exception {
		RTIambassador rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
		debug("joing Federation Execution");
		
		URL[] joinModules = new URL[]{
			(new File(rootPath +  "/foms/zodiac.xml")).toURI().toURL(),
			(new File(rootPath +  "/foms/core.xml")).toURI().toURL(),
			(new File(rootPath +  "/foms/scorpio.xml")).toURI().toURL()
		};
		
		String mac = SystemProperties.getInstance().getMacAddress();
		
		rtiamb.joinFederationExecution( "Scorpio Node " + mac, "ZodiacType", "Zodiac", joinModules );           
	}

	public void releaseAttributeOwnership(ObjectInstanceHandle theObject, AttributeHandleSet candidateAttributes) {
		debug("Release Attribute Ownership Request");
		
		try {
			RTIambassador rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
			rtiamb.attributeOwnershipDivestitureIfWanted( theObject, candidateAttributes );
		} catch ( Exception e ) {
			error("Error: " + e.getMessage() );
		}		
	}
	
	
	
}
