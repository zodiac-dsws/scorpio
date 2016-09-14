package br.com.cmabreu.zodiac.scorpio.services;

import java.util.List;

import br.com.cmabreu.zodiac.scorpio.entity.Instance;
import br.com.cmabreu.zodiac.scorpio.exceptions.DatabaseConnectException;
import br.com.cmabreu.zodiac.scorpio.exceptions.InsertException;
import br.com.cmabreu.zodiac.scorpio.exceptions.NotFoundException;
import br.com.cmabreu.zodiac.scorpio.exceptions.UpdateException;
import br.com.cmabreu.zodiac.scorpio.repository.InstanceRepository;
import br.com.cmabreu.zodiac.scorpio.types.InstanceStatus;

public class InstanceService {
	private InstanceRepository rep;
	
	public InstanceService() throws DatabaseConnectException {
		this.rep = new InstanceRepository();
	}
	
	public void newTransaction() {
		if( !rep.isOpen() ) {
			rep.newTransaction();
		}
	}

	public void close() {
		rep.closeSession();
	}

	public void finishInstance( Instance instance ) throws UpdateException {
		Instance oldInstance;
		try {
			oldInstance = rep.getInstance( instance.getSerial() );
		} catch (NotFoundException e) {
			throw new UpdateException( e.getMessage() );
		}
		
		oldInstance.setStatus(  InstanceStatus.FINISHED );
		oldInstance.setStartDateTime( instance.getStartDateTime() );
		oldInstance.setFinishDateTime( instance.getFinishDateTime() );
		oldInstance.setExecutedBy( instance.getExecutedBy() );
		oldInstance.setCoresUsed( instance.getCoresUsed() );
		oldInstance.setRealFinishTimeMillis( instance.getRealFinishTimeMillis() );
		oldInstance.setRealStartTimeMillis( instance.getRealStartTimeMillis() );

		rep.newTransaction();
		rep.updateInstance(oldInstance);
	}	

	public int insertInstance(Instance instance) throws InsertException {
		rep.insertInstance( instance );
		return 0;
	}
	
	public void insertInstanceList( List<Instance> pipes ) throws InsertException {
		rep.insertInstanceList( pipes );
	}
	
	public Instance getInstance( String serial ) throws NotFoundException {
		return rep.getInstance(serial);
	}

	public Instance getInstance( int idInstance ) throws NotFoundException {
		return rep.getInstance( idInstance );
	}
	
	
	public List<Instance> getList( int idFragment ) throws NotFoundException {
		List<Instance> pipes = rep.getList( idFragment );
		return pipes;
	}

	public List<Instance> getPipelinedList( int idFragment ) throws NotFoundException {
		List<Instance> pipes = rep.getPipelinedList( idFragment );
		return pipes;
	}
	
	public List<Instance> getHead( int howMany, int idFragment ) throws Exception {
		List<Instance> pipes = rep.getHead( howMany, idFragment );
		return pipes;
	}
	
	/*
	public List<Instance> getHeadJoin( int howMany, int idFragment ) throws Exception {
		List<Instance> pipes = rep.getHeadJoin( howMany, idFragment );
		return pipes;
	}
	*/

	public List<Instance> recoverFromCrash( ) throws Exception {
		List<Instance> pipes = rep.recoverFromCrash();
		return pipes;
	}
	
	
}
