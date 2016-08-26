package br.com.cmabreu.zodiac.federation.classes;


import br.com.cmabreu.zodiac.federation.EncoderDecoder;
import br.com.cmabreu.zodiac.federation.RTIAmbassadorProvider;
import br.com.cmabreu.zodiac.federation.objects.ScorpioObject;
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

public class ScorpioClass {
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
	private AttributeHandle totalInstancesHandle;
	
	private AttributeHandleSet attributes;
	
	private EncoderDecoder encodec;	
	private ScorpioObject scorpio;

	public ScorpioObject getScorpio() {
		return scorpio;
	}
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	
	
	public boolean objectExists( ObjectInstanceHandle objHandle ) {
		return scorpio.isMe( objHandle );
	}
	
	public ScorpioObject createNew() throws Exception {
		debug("creating new Scorpio Object instance");
		ObjectInstanceHandle handle = rtiamb.registerObjectInstance( classHandle, "Scorpio Node" );
		scorpio = new ScorpioObject( handle );
		firstUpdateAllAttributeValues( scorpio );
		return scorpio;
	}	
	
	
	public ObjectClassHandle getClassHandle() {
		return classHandle;
	}
	
	public boolean isSameOf( ObjectClassHandle theObjectClass ) {
		return theObjectClass.equals( classHandle );
	}
	
	public ScorpioClass( ) throws Exception {
		rtiamb = RTIAmbassadorProvider.getInstance().getRTIAmbassador();
		this.classHandle = rtiamb.getObjectClassHandle( "HLAobjectRoot.Scorpio" );
		
		this.macAddressHandle = rtiamb.getAttributeHandle( classHandle, "MACAddress" );
		this.soNameHandle = rtiamb.getAttributeHandle( classHandle, "SOName" );
		this.machineNameHandle = rtiamb.getAttributeHandle( classHandle, "MachineName" );
		this.cpuLoadHandle = rtiamb.getAttributeHandle( classHandle, "CpuLoad" );
		this.availableProcessorsHandle = rtiamb.getAttributeHandle( classHandle, "AvailableProcessors" );
		this.totalMemoryHandle = rtiamb.getAttributeHandle( classHandle, "TotalMemory" );
		this.freeMemoryHandle = rtiamb.getAttributeHandle( classHandle, "FreeMemory" );
		this.ipAddressHandle = rtiamb.getAttributeHandle( classHandle, "IPAddress" );
		this.totalInstancesHandle = rtiamb.getAttributeHandle( classHandle, "TotalInstances" );
		
		this.attributes = rtiamb.getAttributeHandleSetFactory().create();
		attributes.add( macAddressHandle );
		attributes.add( soNameHandle );
		attributes.add( machineNameHandle );
		attributes.add( cpuLoadHandle );
		attributes.add( availableProcessorsHandle );
		attributes.add( totalMemoryHandle );
		attributes.add( freeMemoryHandle );
		attributes.add( ipAddressHandle );
		attributes.add( totalInstancesHandle );
		
		encodec = new EncoderDecoder();	
	}

	private void firstUpdateAllAttributeValues( ScorpioObject scorpio ) throws RTIexception {
		debug("updating all attributes for first time");

		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(8);
		HLAunicodeString macAddressValue = encodec.createHLAunicodeString( scorpio.getMacAddress() );
		HLAunicodeString soNameValue = encodec.createHLAunicodeString( scorpio.getSoName() );
		HLAunicodeString machineNameValue = encodec.createHLAunicodeString( scorpio.getMachineName() );
		HLAfloat64BE cpuLoadValue = encodec.createHLAfloat64BE( scorpio.getCpuLoad() );
		HLAinteger32BE availableProcessorsValue = encodec.createHLAinteger32BE( scorpio.getAvailableProcessors() );
		HLAinteger64BE totalMemoryValue = encodec.createHLAinteger64BE( scorpio.getTotalMemory() );
		HLAinteger64BE freeMemoryValue = encodec.createHLAinteger64BE( scorpio.getFreeMemory() );
		HLAunicodeString ipAddressValue = encodec.createHLAunicodeString( scorpio.getIpAddress() );

		HLAinteger32BE totalInstancesValue = encodec.createHLAinteger32BE( scorpio.getTotalInstances() );

		attributes.put( macAddressHandle, macAddressValue.toByteArray() );
		attributes.put( soNameHandle, soNameValue.toByteArray() );
		attributes.put( machineNameHandle, machineNameValue.toByteArray() );
		attributes.put( cpuLoadHandle, cpuLoadValue.toByteArray() );
		attributes.put( availableProcessorsHandle, availableProcessorsValue.toByteArray() );
		attributes.put( totalMemoryHandle, totalMemoryValue.toByteArray() );
		attributes.put( freeMemoryHandle, freeMemoryValue.toByteArray() );
		attributes.put( ipAddressHandle, ipAddressValue.toByteArray() );
		attributes.put( totalInstancesHandle, totalInstancesValue.toByteArray() );
		
		rtiamb.updateAttributeValues( scorpio.getHandle(), attributes, "Scorpio Attributes".getBytes() );
	}	
	

	// Just need to update attributes that changes
	public void updateAttributeValues( ) throws RTIexception {
		scorpio.updateValues();
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(3);
		HLAfloat64BE cpuLoadValue = encodec.createHLAfloat64BE( scorpio.getCpuLoad() );
		HLAinteger64BE totalMemoryValue = encodec.createHLAinteger64BE( scorpio.getTotalMemory() );
		HLAinteger64BE freeMemoryValue = encodec.createHLAinteger64BE( scorpio.getFreeMemory() );
		HLAinteger32BE totalInstancesValue = encodec.createHLAinteger32BE( scorpio.getTotalInstances() );
		attributes.put( cpuLoadHandle, cpuLoadValue.toByteArray() );
		attributes.put( totalMemoryHandle, totalMemoryValue.toByteArray() );
		attributes.put( freeMemoryHandle, freeMemoryValue.toByteArray() );
		attributes.put( totalInstancesHandle, totalInstancesValue.toByteArray() );
		rtiamb.updateAttributeValues( scorpio.getHandle(), attributes, "Scorpio Attributes".getBytes() );
	}	
	
	public void publish() throws RTIexception {
		debug("publish Scorpio attributes");
		rtiamb.publishObjectClassAttributes( classHandle, attributes );
	}
	
	public void subscribe() throws RTIexception {
		debug("subscribe to Scorpio attributes");
		rtiamb.subscribeObjectClassAttributes( classHandle, attributes );		
	}

	public void increaseTotalInstances() {
		scorpio.increaseTotalInstances();
	}

	
}
