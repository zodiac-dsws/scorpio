package br.com.cmabreu.zodiac.federation.classes;


import br.com.cmabreu.zodiac.federation.EncoderDecoder;
import br.com.cmabreu.zodiac.federation.RTIAmbassadorProvider;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.HLAunicodeString;

public class RequestTaskInteractionClass {
	private InteractionClassHandle requestTaskInteractionHandle;
	private RTIambassador rtiamb;
	private ParameterHandle coreSerialNumberParameterHandle;
	private ParameterHandle taskTypeParameterHandle;
	private EncoderDecoder encodec;
	
	public boolean isMe( InteractionClassHandle interactionClass ) {
		return interactionClass.equals( requestTaskInteractionHandle ); 
	}

	public RequestTaskInteractionClass() throws Exception {
		rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
		requestTaskInteractionHandle = rtiamb.getInteractionClassHandle( "HLAinteractionRoot.RequestTask" );
		coreSerialNumberParameterHandle = rtiamb.getParameterHandle(requestTaskInteractionHandle, "CoreSerialNumber");
		taskTypeParameterHandle = rtiamb.getParameterHandle(requestTaskInteractionHandle, "TaskType");
		encodec = new EncoderDecoder();
	}
	
	public String getNodeSerial( ParameterHandleValueMap parameters ) {
		String coreSerial = encodec.toString( parameters.get( coreSerialNumberParameterHandle ) );
		return coreSerial;
	}
	
	public String getTaskType( ParameterHandleValueMap parameters ) {
		String taskType = encodec.toString( parameters.get( taskTypeParameterHandle ) );
		return taskType;
	}

	public void publish() throws Exception {
		rtiamb.publishInteractionClass( requestTaskInteractionHandle );
	}
	
	public void subscribe() throws Exception {
		rtiamb.subscribeInteractionClass( requestTaskInteractionHandle );		
	}
	
	public void send( String targetCore, String taskType ) throws Exception {
		HLAunicodeString coreValue = encodec.createHLAunicodeString( targetCore );
		HLAunicodeString taskTypeValue = encodec.createHLAunicodeString( taskType );
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);
		parameters.put( coreSerialNumberParameterHandle, coreValue.toByteArray() );
		parameters.put( taskTypeParameterHandle, taskTypeValue.toByteArray() );
		rtiamb.sendInteraction( requestTaskInteractionHandle, parameters, "Request Task".getBytes() );
	}

}
