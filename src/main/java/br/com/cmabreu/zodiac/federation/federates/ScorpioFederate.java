package br.com.cmabreu.zodiac.federation.federates;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import br.com.cmabreu.zodiac.federation.EncoderDecoder;
import br.com.cmabreu.zodiac.federation.Environment;
import br.com.cmabreu.zodiac.federation.RTIAmbassadorProvider;
import br.com.cmabreu.zodiac.federation.classes.CoreClass;
import br.com.cmabreu.zodiac.federation.classes.ScorpioClass;
import br.com.cmabreu.zodiac.federation.objects.CoreObject;
import br.com.cmabreu.zodiac.federation.objects.ScorpioObject;
import br.com.cmabreu.zodiac.scorpio.Logger;
import br.com.cmabreu.zodiac.scorpio.misc.SystemProperties;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectInstanceHandle;
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

	public void updateWorkingDataCore( CoreObject core ) throws Exception {
		coreClass.updateWorkingDataCore( core );
	}
	
	public void updateWorkingData( ) throws Exception {
		coreClass.updateWorkingData( );
	}
	
	public void startFederate() throws Exception {
		
		debug("starting Scorpio...");

		scorpioClass = new ScorpioClass();
		scorpioClass.publish();
		ScorpioObject to = scorpioClass.createNew();

		// Publish Cores attributes 
		coreClass = new CoreClass();
		coreClass.publish();
		// Subscribe to CurrentInstance updates
		coreClass.subscribeToCurrentInstance();

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
			(new File(rootPath +  "/foms/sagittarius.xml")).toURI().toURL(),
			(new File(rootPath +  "/foms/core.xml")).toURI().toURL(),
			(new File(rootPath +  "/foms/gemini.xml")).toURI().toURL(),
			(new File(rootPath +  "/foms/scorpio.xml")).toURI().toURL()
		};
		
		String mac = SystemProperties.getInstance().getMacAddress();
		
		rtiamb.joinFederationExecution( "Scorpio Node " + mac, "ScorpioType", "Zodiac", joinModules );           
	}

	public void releaseAttributeOwnership(ObjectInstanceHandle theObject, AttributeHandleSet candidateAttributes) {
		CoreObject core = coreClass.getCore( theObject );
		debug("Received request to release ownership of Core " + core.getSerial() );
		
		try {
			RTIambassador rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
			rtiamb.attributeOwnershipDivestitureIfWanted( theObject, candidateAttributes );
			debug("Ownership of Core " + core.getSerial() + " released.");
		} catch ( Exception e ) {
			error("Error: " + e.getMessage() );
		}		
	}
	
	// The attribute "CurrentInstance" ownership was took. WIll run the instance.
	public void attributeOwnershipAcquisitionNotification( ObjectInstanceHandle theObject, AttributeHandleSet securedAttributes ) {
		try {
			coreClass.processInstance( theObject );
			scorpioClass.increaseTotalInstances();
		} catch ( Exception e ) {
			error("Cannot execute instance: " + e.getMessage() );
			e.printStackTrace();
		}
	}
	
	private String getHexInstance( AttributeHandleValueMap theAttributes, CoreObject core ) throws Exception {
		EncoderDecoder encodec = new EncoderDecoder();
		String instance = "";
		for( AttributeHandle attributeHandle : theAttributes.keySet() )	{
			if( attributeHandle.equals( coreClass.getCurrentInstanceHandle() ) ) {
				instance = encodec.toString( theAttributes.get( attributeHandle) );
				break;
			}
		}
		return instance;
	}	
	
	private synchronized void takeBackCurrentInstanceOwnership( ObjectInstanceHandle theObject,  AttributeHandleValueMap theAttributes ) throws Exception {
		boolean found = false;
		for ( CoreObject core : coreClass.getCores() ) {
			if ( core.isMe( theObject )  ) {
				found = true;
				if ( !core.isWorking() ) {
					core.setCurrentInstance( getHexInstance( theAttributes, core ) );
					coreClass.requestOwnershipBack( core );
				} else {
					error("Too fast! Core " + core.getSerial() + " still working.");
				}
				break;
			}
		}
		if ( !found ) {
			error("Cannot find a valid core to process this instance.");
		}
	}
	
	public void reflectAttributeValues( ObjectInstanceHandle theObject,  AttributeHandleValueMap theAttributes ) {
		
		try {
			debug("Attribute update incommig...");
			takeBackCurrentInstanceOwnership(theObject, theAttributes);
		} catch ( Exception e ) {
			e.printStackTrace(); 
		}		
		
	}

	public void reportOwnershipUnavailable(ObjectInstanceHandle theObject, AttributeHandleSet theAttributes) {
		/*
		for ( CoreObject core : coreClass.getCores() ) {
			if ( core.isMe( theObject )  ) {
				debug( core.getSerial() + "@" + core.getOwnerNode() + ": attribute not available yet.");
				break;
			}
		}
		*/
	}
	
	
}
