package br.com.cmabreu.zodiac.federation;

import br.com.cmabreu.zodiac.federation.federates.TeapotFederate;
import br.com.cmabreu.zodiac.scorpio.Logger;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;


public class SagitariiAmbassador extends NullFederateAmbassador {

	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	

	private void warn( String s ) {
		Logger.getInstance().warn(this.getClass().getName(), s );
	}	

	private void error( String s ) {
		Logger.getInstance().error(this.getClass().getName(), s );
	}	

	@Override
	public void reflectAttributeValues( ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
	                                    byte[] tag, OrderType sentOrder, TransportationTypeHandle transport,
	                                    SupplementalReflectInfo reflectInfo ) throws FederateInternalError {
		reflectAttributeValues( theObject, theAttributes, tag, sentOrder, transport, null, sentOrder, reflectInfo );
	}

	
	@Override
	public void provideAttributeValueUpdate(ObjectInstanceHandle theObject,	AttributeHandleSet theAttributes, byte[] userSuppliedTag)
			throws FederateInternalError {

		try {
			if ( TeapotFederate.getInstance().getCoreClass().objectExists( theObject ) ) {
				TeapotFederate.getInstance().getCoreClass().provideAttributeValueUpdate(theObject, theAttributes);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}	
	
	@Override
	public void discoverObjectInstance( ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass, String objectName ) throws FederateInternalError {
		try {
			if ( TeapotFederate.getInstance().getSagitariiClass().isSameOf( theObjectClass ) ) {
				try {
					debug("Sagitarii is online!");
					TeapotFederate.getInstance().sagitariiIsUp();
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			
		} catch ( Exception e ) {
			error( e.getMessage() );
		}
	}
	
	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject, byte[] userSuppliedTag, OrderType sentOrdering, SupplementalRemoveInfo removeInfo)	{
		try {
			if ( TeapotFederate.getInstance().getSagitariiClass().objectExists(theObject) ) {
				try {
					debug("Remove Sagitarii object. Sagitarii is offline! ");
					TeapotFederate.getInstance().sagitariiIsDown();
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}		
	}	

	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass,	ParameterHandleValueMap theParameters,
			byte[] userSuppliedTag, OrderType sentOrdering,	TransportationTypeHandle theTransport, 
			SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
		
		try {
			TeapotFederate.getInstance().processInstance( theParameters );
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}
	
	@Override
	public void connectionLost(String arg0) throws FederateInternalError {
		warn("  *** connectionLost ");
	}	
}
