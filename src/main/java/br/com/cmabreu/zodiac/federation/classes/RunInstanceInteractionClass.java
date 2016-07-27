package br.com.cmabreu.zodiac.federation.classes;


import br.com.cmabreu.zodiac.federation.EncoderDecoder;
import br.com.cmabreu.zodiac.federation.RTIAmbassadorProvider;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.HLAunicodeString;

public class RunInstanceInteractionClass {
	private InteractionClassHandle runInstanceInteractionHandle;
	private ParameterHandle instanceContentParameterHandle;
	private ParameterHandle coreSerialNumberParameterHandle;
	private EncoderDecoder encodec;
	private RTIambassador rtiamb;

	public boolean isMe( InteractionClassHandle interactionClass ) {
		return interactionClass.equals( runInstanceInteractionHandle ); 
	}
	
	public class InstanceCommandPack {
		public String targetCore;
		public String instanceHex;
	}
	
	public RunInstanceInteractionClass() throws Exception {
		rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
		runInstanceInteractionHandle = rtiamb.getInteractionClassHandle( "HLAinteractionRoot.RunInstance" );
		instanceContentParameterHandle = rtiamb.getParameterHandle(runInstanceInteractionHandle, "InstanceContent");
		coreSerialNumberParameterHandle = rtiamb.getParameterHandle(runInstanceInteractionHandle, "CoreSerialNumber");
		encodec = new EncoderDecoder();
	}

	public void publish() throws Exception {
		rtiamb.publishInteractionClass( runInstanceInteractionHandle );
	}
	
	public InstanceCommandPack getInstanceHexDef( ParameterHandleValueMap parameters ) {
		InstanceCommandPack icp = null;
		try {
			String instanceHexDef = encodec.toString( parameters.get( instanceContentParameterHandle ) );
			String targetCore = encodec.toString( parameters.get( coreSerialNumberParameterHandle ) );
			if ( ( !instanceHexDef.equals("") ) && ( !targetCore.equals("") ) ) {
				icp = new InstanceCommandPack();
				icp.instanceHex = instanceHexDef;
				icp.targetCore = targetCore;
			}
		} catch ( Exception e ) { }
		return icp;
	}	
	
	public void subscribe() throws Exception {
		rtiamb.subscribeInteractionClass( runInstanceInteractionHandle );		
	}
	
	public void send( String instance, String targetCore ) throws Exception {
		HLAunicodeString instanceValue = encodec.createHLAunicodeString( instance );
		HLAunicodeString coreValue = encodec.createHLAunicodeString( targetCore );
		
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);
		parameters.put( instanceContentParameterHandle, instanceValue.toByteArray() );
		parameters.put( coreSerialNumberParameterHandle, coreValue.toByteArray() );
		rtiamb.sendInteraction( runInstanceInteractionHandle, parameters, "Run Instance".getBytes() );		
	}
	
}
