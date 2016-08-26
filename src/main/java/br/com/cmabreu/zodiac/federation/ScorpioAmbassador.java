package br.com.cmabreu.zodiac.federation;

import br.com.cmabreu.zodiac.federation.federates.ScorpioFederate;
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


public class ScorpioAmbassador extends NullFederateAmbassador {


	private void warn( String s ) {
		Logger.getInstance().warn(this.getClass().getName(), s );
	}	


	@Override
	public void attributeOwnershipUnavailable( ObjectInstanceHandle theObject, AttributeHandleSet theAttributes ) {
		try {
			ScorpioFederate.getInstance().reportOwnershipUnavailable( theObject, theAttributes );
		} catch ( Exception e ) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public void reflectAttributeValues( ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes,
	                                    byte[] tag, OrderType sentOrder, TransportationTypeHandle transport,
	                                    SupplementalReflectInfo reflectInfo ) throws FederateInternalError {
		try {
			ScorpioFederate.getInstance().reflectAttributeValues( theObject, theAttributes );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	
	@Override
	public void provideAttributeValueUpdate(ObjectInstanceHandle theObject,	AttributeHandleSet theAttributes, byte[] userSuppliedTag)
			throws FederateInternalError {

		try {
			if ( ScorpioFederate.getInstance().getCoreClass().objectExists( theObject ) ) {
				ScorpioFederate.getInstance().getCoreClass().provideAttributeValueUpdate(theObject, theAttributes);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}	
	
	@Override
	public void discoverObjectInstance( ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass, String objectName ) throws FederateInternalError {
		//
	}
	
	
	@Override
	public void attributeOwnershipAcquisitionNotification(	ObjectInstanceHandle theObject,	AttributeHandleSet securedAttributes, byte[] userSuppliedTag) throws FederateInternalError {
		try {
			ScorpioFederate.getInstance().attributeOwnershipAcquisitionNotification( theObject, securedAttributes );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}	

	
	@Override
    public void requestAttributeOwnershipRelease( ObjectInstanceHandle theObject, AttributeHandleSet candidateAttributes, byte[] userSuppliedTag) throws FederateInternalError {
		try {
			ScorpioFederate.getInstance().releaseAttributeOwnership(theObject, candidateAttributes);
		} catch ( Exception e ) {
			e.printStackTrace();
		}
    }	
	
	@Override
	public void removeObjectInstance(ObjectInstanceHandle theObject, byte[] userSuppliedTag, OrderType sentOrdering, SupplementalRemoveInfo removeInfo)	{
		//
	}	

	@Override
	public void receiveInteraction(InteractionClassHandle interactionClass,	ParameterHandleValueMap theParameters,
			byte[] userSuppliedTag, OrderType sentOrdering,	TransportationTypeHandle theTransport, 
			SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
		
		// 
		
	}
	
	@Override
	public void connectionLost(String arg0) throws FederateInternalError {
		warn("  *** connectionLost ");
	}	
}
