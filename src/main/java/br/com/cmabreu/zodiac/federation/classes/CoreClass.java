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
	
	
	private AttributeHandleSet attributes;
	private List<CoreObject> objects;
	private EncoderDecoder encodec;
	
	
	public List<CoreObject> getCores() {
		return new ArrayList<CoreObject>(objects);
	}
	
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}
	
	public ObjectInstanceHandle createNew( String ownerNode ) throws RTIexception {
		debug("new HLA Object instance to node " + ownerNode );
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
	
	public void requestInstances() throws Exception {
		if( !isIdle() ) return;
		for ( CoreObject core : getCores() ) {
			core.requestTask();
		}
	}
	
	public synchronized void processInstance( String coreSerial, String instance ) throws Exception {
		for ( CoreObject core : getCores() ) {
			if ( core.getSerial().equals( coreSerial ) ) {
				core.process( instance );
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
		
		rtiamb.updateAttributeValues( core.getHandle(), attributes, "Core Working Data".getBytes() );
	}	
	
	public void updateWorkingData() throws Exception {
		for ( CoreObject core : getCores()  ) {
			updateWorkingDataCore( core );
		}
	}
	
	
	public void provideAttributeValueUpdate(ObjectInstanceHandle theObject, AttributeHandleSet theAttributes )  {
		debug("Update attribute request for Core " + theObject);
		for ( CoreObject core : getCores() ) {
			if( core.isMe( theObject ) ) {
				try {
					updateAttributeValuesObject( core );
				} catch ( Exception e ) {
					e.printStackTrace();
				}
				return;
			}
		}
	}	
	
	public void updateAttributeValues() throws RTIexception {
		for ( CoreObject object : getCores() ) {
			updateAttributeValuesObject( object );
		}
	}
	
	public void updateAttributeValuesObject( CoreObject object ) throws RTIexception {
		
		HLAunicodeString serialNumberValue = encodec.createHLAunicodeString( object.getSerial() );
		HLAunicodeString ownerNodeValue = encodec.createHLAunicodeString( object.getOwnerNode() );
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(3);
		attributes.put( serialNumberHandle, serialNumberValue.toByteArray() );
		
		attributes.put( ownerNodeHandle, ownerNodeValue.toByteArray() );
		rtiamb.updateAttributeValues( object.getHandle(), attributes, "Core Attributes".getBytes() );
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
		
		objects = new ArrayList<CoreObject>();
		encodec = new EncoderDecoder();
	}
	
	public void publish() throws RTIexception {
		debug("publish");
		rtiamb.publishObjectClassAttributes( classHandle, attributes );
	}
	
	public void subscribe() throws RTIexception {
		debug("subscribe");
		rtiamb.subscribeObjectClassAttributes( classHandle, attributes );		
	}

	
	
}
