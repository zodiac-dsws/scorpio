package br.com.cmabreu.zodiac.federation.classes;

import java.util.ArrayList;
import java.util.List;

import br.com.cmabreu.zodiac.federation.EncoderDecoder;
import br.com.cmabreu.zodiac.federation.RTIAmbassadorProvider;
import br.com.cmabreu.zodiac.federation.objects.CoreObject;
import br.com.cmabreu.zodiac.scorpio.Logger;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIexception;

public class CoreClass {
	private RTIambassador rtiamb;
	private ObjectClassHandle classHandle;
	private AttributeHandle isWorkingHandle;
	private AttributeHandle serialNumberHandle;
	private AttributeHandle ownerNodeHandle;
	
	private AttributeHandle experimentSerialHandle;
	private AttributeHandle fragmentSerialHandle;
	private AttributeHandle instanceSerialHandle;
	private AttributeHandle activitySerialHandle;
	private AttributeHandle executorHandle;
	private AttributeHandle executorTypeHandle;
	private AttributeHandle currentInstanceHandle;
	private AttributeHandle executionResultHandle;
	
	private AttributeHandleSet attributes;
	private List<CoreObject> objects;
	private EncoderDecoder encodec;
	
	public AttributeHandle getCurrentInstanceHandle() {
		return currentInstanceHandle;
	}
	
	public List<CoreObject> getCores() {
		return new ArrayList<CoreObject>(objects);
	}
	
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}
	
	public ObjectInstanceHandle createNew( String ownerNode ) throws RTIexception {
		debug("Core found in node " + ownerNode );
		ObjectInstanceHandle coreObjectHandle = rtiamb.registerObjectInstance( classHandle );
		CoreObject co = new CoreObject( coreObjectHandle );
		co.setOwnerNode(ownerNode);
		objects.add( co );
		return coreObjectHandle;
	}
	
	public boolean isIdle() {
		boolean result = false; 
		for ( CoreObject core : getCores() ) {
			if ( core.isWorking() ) result = true;
		}
		return result;
	}
	
	
	public void requestOwnershipBack( CoreObject core ) throws Exception {
		debug("Request attribute back: " + core.getSerial() );
		RTIambassador rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
		AttributeHandleSet ahs = rtiamb.getAttributeHandleSetFactory().create();
		ahs.add( currentInstanceHandle );
		
		rtiamb.attributeOwnershipAcquisition( core.getHandle(), ahs, "Getting back my attribute".getBytes() );
		
		// Will try to get back even when Sagittarius was gone. The attribute will be unowned.
		rtiamb.attributeOwnershipAcquisitionIfAvailable( core.getHandle(), ahs );
	}	
	
	public synchronized void processInstance( ObjectInstanceHandle theObject ) throws Exception {
		for ( CoreObject core : getCores() ) {
			if ( core.isMe( theObject )  ) {
				core.process( core.getCurrentInstance() );
				debug("Core " + core.getSerial() + " finished instance " + core.getInstanceSerial() );
				updateWorkingDataCore( core );
				break;
			}
		}
	}		
	
	public void updateWorkingDataCore( CoreObject core ) throws Exception {
		HLAunicodeString experimentSerialValue = encodec.createHLAunicodeString( core.getExperimentSerial() );
		HLAunicodeString fragmentSerialValue = encodec.createHLAunicodeString( core.getFragmentSerial() );
		HLAunicodeString instanceSerialValue = encodec.createHLAunicodeString( core.getInstanceSerial() );
		HLAunicodeString activitySerialValue = encodec.createHLAunicodeString( core.getActivitySerial() );
		HLAunicodeString executorValue = encodec.createHLAunicodeString( core.getExecutor() );
		HLAunicodeString executorTypeValue = encodec.createHLAunicodeString( core.getExecutorType() );
		HLAunicodeString currentInstanceHandleValue = encodec.createHLAunicodeString( core.getCurrentInstance() );
		HLAinteger32BE executionResultValue = encodec.createHLAinteger32BE( core.getResult() );
		HLAboolean isWorkingValue = encodec.createHLAboolean( core.isWorking() );
		
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(5);
		
		attributes.put( experimentSerialHandle, experimentSerialValue.toByteArray() );
		attributes.put( fragmentSerialHandle, fragmentSerialValue.toByteArray() );
		attributes.put( instanceSerialHandle, instanceSerialValue.toByteArray() );
		attributes.put( activitySerialHandle, activitySerialValue.toByteArray() );
		attributes.put( executorHandle, executorValue.toByteArray() );
		attributes.put( executorTypeHandle, executorTypeValue.toByteArray() );
		attributes.put( isWorkingHandle, isWorkingValue.toByteArray() );
		attributes.put( currentInstanceHandle, currentInstanceHandleValue.toByteArray() );
		attributes.put( executionResultHandle, executionResultValue.toByteArray() );
		
		rtiamb.updateAttributeValues( core.getHandle(), attributes, "Core Working Data".getBytes() );
	}	
	
	public void updateWorkingData() throws Exception {
		for ( CoreObject core : getCores()  ) {
			updateWorkingDataCore( core );
		}
	}
	
	private void error( String s ) {
		Logger.getInstance().error( this.getClass().getName(), s );
	}		
	
	public void provideAttributeValueUpdate(ObjectInstanceHandle theObject, AttributeHandleSet theAttributes )  {
		debug("Update attribute request for Core " + theObject);
		for ( CoreObject core : getCores() ) {
			if( core.isMe( theObject ) ) {
				try {
					sendCoreInitialState( core );
				} catch ( Exception e ) {
					error("Provide Attribute Update Error: " + e.getMessage() );
				}
				return;
			}
		}
	}	
	
	/*
	public void updateAttributeValues() throws RTIexception {
		for ( CoreObject object : getCores() ) {
			updateAttributeValuesObject( object );
		}
	}
	*/

	// Sent under request
	private void sendCoreInitialState( CoreObject core ) throws RTIexception {
		HLAunicodeString serialNumberValue = encodec.createHLAunicodeString( core.getSerial() );
		HLAunicodeString ownerNodeValue = encodec.createHLAunicodeString( core.getOwnerNode() );
		HLAunicodeString currentInstanceHandleValue = encodec.createHLAunicodeString( core.getCurrentInstance() );		
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(3);
		attributes.put( serialNumberHandle, serialNumberValue.toByteArray() );
		attributes.put( ownerNodeHandle, ownerNodeValue.toByteArray() );
		attributes.put( currentInstanceHandle, currentInstanceHandleValue.toByteArray() );
		rtiamb.updateAttributeValues( core.getHandle(), attributes, "Core Initial State".getBytes() );
	}
	
	public boolean objectExists( ObjectInstanceHandle objHandle ) {
		for ( CoreObject object : getCores()  ) {
			if ( object.isMe( objHandle ) ) {
				return true;
			}
		}
		return false;
	}
	
	public ObjectClassHandle getClassHandle() {
		return classHandle;
	}
	
	public boolean isSameOf( ObjectClassHandle theObjectClass ) {
		return theObjectClass.equals( classHandle );
	}
	
	public CoreClass() throws Exception {
		rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
		this.classHandle = rtiamb.getObjectClassHandle( "HLAobjectRoot.Core" );
		
		this.isWorkingHandle = rtiamb.getAttributeHandle( classHandle, "IsWorking" );
		this.serialNumberHandle = rtiamb.getAttributeHandle( classHandle, "SerialNumber" );
		this.ownerNodeHandle = rtiamb.getAttributeHandle( classHandle, "OwnerNode" );

		this.experimentSerialHandle = rtiamb.getAttributeHandle( classHandle, "ExperimentSerial" );
		this.fragmentSerialHandle = rtiamb.getAttributeHandle( classHandle, "FragmentSerial" );
		this.instanceSerialHandle = rtiamb.getAttributeHandle( classHandle, "InstanceSerial" );
		this.activitySerialHandle = rtiamb.getAttributeHandle( classHandle, "ActivitySerial" );
		this.currentInstanceHandle = rtiamb.getAttributeHandle( classHandle, "CurrentInstance" );
		this.executionResultHandle = rtiamb.getAttributeHandle( classHandle, "LastExecutionResult" );
		
		this.executorHandle = rtiamb.getAttributeHandle( classHandle, "Executor" );
		this.executorTypeHandle = rtiamb.getAttributeHandle( classHandle, "ExecutorType" );
		
		this.attributes = rtiamb.getAttributeHandleSetFactory().create();
		attributes.add( isWorkingHandle );
		attributes.add( serialNumberHandle );
		attributes.add( ownerNodeHandle );
		attributes.add( experimentSerialHandle );
		attributes.add( fragmentSerialHandle );
		attributes.add( instanceSerialHandle );
		attributes.add( activitySerialHandle );
		attributes.add( executorHandle );
		attributes.add( executorTypeHandle );
		attributes.add( currentInstanceHandle );
		attributes.add( executionResultHandle );
		
		objects = new ArrayList<CoreObject>();
		encodec = new EncoderDecoder();
	}
	
	public void publish() throws RTIexception {
		debug("publish Core attributes");
		rtiamb.publishObjectClassAttributes( classHandle, attributes );
	}
	
	public void subscribeToCurrentInstance() throws RTIexception {
		debug("subscribe to Core current instance attribute");
		AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
		attributes.add( currentInstanceHandle );
		rtiamb.subscribeObjectClassAttributes( classHandle, attributes );		
	}

	
	
}
