package br.com.cmabreu.zodiac.federation.classes;


import br.com.cmabreu.zodiac.federation.EncoderDecoder;
import br.com.cmabreu.zodiac.federation.RTIAmbassadorProvider;
import br.com.cmabreu.zodiac.federation.objects.TeapotObject;
import br.com.cmabreu.zodiac.scorpio.Logger;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.encoding.HLAfloat64BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAinteger64BE;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIexception;

public class TeapotClass {
	private ObjectClassHandle classHandle;
	private RTIambassador rtiamb;
	
	private AttributeHandle macAddressHandle;
	private AttributeHandle soNameHandle;
	private AttributeHandle machineNameHandle;
	private AttributeHandle cpuLoadHandle;
	private AttributeHandle availableProcessorsHandle;
	private AttributeHandle totalMemoryHandle;
	private AttributeHandle freeMemoryHandle;
	private AttributeHandle ipAddressHandle;
	
	private AttributeHandleSet attributes;
	
	private EncoderDecoder encodec;	
	private TeapotObject teapot;

	public TeapotObject getTeapot() {
		return teapot;
	}
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	
	
	public boolean objectExists( ObjectInstanceHandle objHandle ) {
		return teapot.isMe( objHandle );
	}
	
	public TeapotObject createNew() throws Exception {
		debug("creating new Teapot Object instance");
		ObjectInstanceHandle handle = rtiamb.registerObjectInstance( classHandle, "Teapot Node" );
		teapot = new TeapotObject( handle );
		firstUpdateAllAttributeValues( teapot );
		return teapot;
	}	
	
	
	public ObjectClassHandle getClassHandle() {
		return classHandle;
	}
	
	public boolean isSameOf( ObjectClassHandle theObjectClass ) {
		return theObjectClass.equals( classHandle );
	}
	
	public TeapotClass( ) throws Exception {
		rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
		this.classHandle = rtiamb.getObjectClassHandle( "HLAobjectRoot.Teapot" );
		
		this.macAddressHandle = rtiamb.getAttributeHandle( classHandle, "MACAddress" );
		this.soNameHandle = rtiamb.getAttributeHandle( classHandle, "SOName" );
		this.machineNameHandle = rtiamb.getAttributeHandle( classHandle, "MachineName" );
		this.cpuLoadHandle = rtiamb.getAttributeHandle( classHandle, "CpuLoad" );
		this.availableProcessorsHandle = rtiamb.getAttributeHandle( classHandle, "AvailableProcessors" );
		this.totalMemoryHandle = rtiamb.getAttributeHandle( classHandle, "TotalMemory" );
		this.freeMemoryHandle = rtiamb.getAttributeHandle( classHandle, "FreeMemory" );
		this.ipAddressHandle = rtiamb.getAttributeHandle( classHandle, "IPAddress" );
		
		this.attributes = rtiamb.getAttributeHandleSetFactory().create();
		attributes.add( macAddressHandle );
		attributes.add( soNameHandle );
		attributes.add( machineNameHandle );
		attributes.add( cpuLoadHandle );
		attributes.add( availableProcessorsHandle );
		attributes.add( totalMemoryHandle );
		attributes.add( freeMemoryHandle );
		attributes.add( ipAddressHandle );
		
		encodec = new EncoderDecoder();	
	}

	private void firstUpdateAllAttributeValues( TeapotObject object ) throws RTIexception {
		debug("updating all attributes for first time");

		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(8);
		HLAunicodeString macAddressValue = encodec.createHLAunicodeString( object.getMacAddress() );
		HLAunicodeString soNameValue = encodec.createHLAunicodeString( object.getSoName() );
		HLAunicodeString machineNameValue = encodec.createHLAunicodeString( object.getMachineName() );
		HLAfloat64BE cpuLoadValue = encodec.createHLAfloat64BE( object.getCpuLoad() );
		HLAinteger32BE availableProcessorsValue = encodec.createHLAinteger32BE( object.getAvailableProcessors() );
		HLAinteger64BE totalMemoryValue = encodec.createHLAinteger64BE( object.getTotalMemory() );
		HLAinteger64BE freeMemoryValue = encodec.createHLAinteger64BE( object.getFreeMemory() );
		HLAunicodeString ipAddressValue = encodec.createHLAunicodeString( object.getIpAddress() );

		attributes.put( macAddressHandle, macAddressValue.toByteArray() );
		attributes.put( soNameHandle, soNameValue.toByteArray() );
		attributes.put( machineNameHandle, machineNameValue.toByteArray() );
		attributes.put( cpuLoadHandle, cpuLoadValue.toByteArray() );
		attributes.put( availableProcessorsHandle, availableProcessorsValue.toByteArray() );
		attributes.put( totalMemoryHandle, totalMemoryValue.toByteArray() );
		attributes.put( freeMemoryHandle, freeMemoryValue.toByteArray() );
		attributes.put( ipAddressHandle, ipAddressValue.toByteArray() );
		
		rtiamb.updateAttributeValues( object.getHandle(), attributes, "Teapot Attributes".getBytes() );
	}	
	

	// Just need to update attributes that changes
	public void updateAttributeValues( ) throws RTIexception {
		teapot.updateValues();
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(3);
		HLAfloat64BE cpuLoadValue = encodec.createHLAfloat64BE( teapot.getCpuLoad() );
		HLAinteger64BE totalMemoryValue = encodec.createHLAinteger64BE( teapot.getTotalMemory() );
		HLAinteger64BE freeMemoryValue = encodec.createHLAinteger64BE( teapot.getFreeMemory() );
		attributes.put( cpuLoadHandle, cpuLoadValue.toByteArray() );
		attributes.put( totalMemoryHandle, totalMemoryValue.toByteArray() );
		attributes.put( freeMemoryHandle, freeMemoryValue.toByteArray() );
		rtiamb.updateAttributeValues( teapot.getHandle(), attributes, "Teapot Attributes".getBytes() );
	}	
	
	public void publish() throws RTIexception {
		rtiamb.publishObjectClassAttributes( classHandle, attributes );
	}
	
	public void subscribe() throws RTIexception {
		rtiamb.subscribeObjectClassAttributes( classHandle, attributes );		
	}

	
}
