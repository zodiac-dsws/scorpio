package br.com.cmabreu.zodiac.scorpio.services;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.cmabreu.zodiac.scorpio.Logger;
import br.com.cmabreu.zodiac.scorpio.UserTableEntity;
import br.com.cmabreu.zodiac.scorpio.entity.Relation;
import br.com.cmabreu.zodiac.scorpio.exceptions.DatabaseConnectException;
import br.com.cmabreu.zodiac.scorpio.exceptions.NotFoundException;
import br.com.cmabreu.zodiac.scorpio.repository.RelationRepository;


public class RelationService { 
	private RelationRepository rep;
	
	public RelationService() throws DatabaseConnectException {
		this.rep = new RelationRepository();
	}
	
	public void executeQuery(String query) throws Exception {
		if ( !rep.isOpen() ) {
			newTransaction();
		}
		rep.executeQuery(query);
	}	
	
	public void newTransaction() {
		if ( !rep.isOpen() ) {
			rep.newTransaction();
		}
	}	
	
	// Get the triad id_activity + id_experiment + id_instance given an Instance serial.
	public UserTableEntity getTriad( String instanceSerial ) throws Exception {
		String query = "select act.id_activity, frag.id_experiment, inst.id_instance from activities act " + 
				"join fragments frag on frag.id_fragment = act.id_fragment " +
				"join instances inst on inst.id_fragment = frag.id_fragment where inst.serial = '" + instanceSerial + "'";
		List<UserTableEntity> result = new ArrayList<UserTableEntity>( genericFetchList(query) );
		if ( result.size() > 0 ) return result.get( 0 );
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public Set<UserTableEntity> genericFetchList(String query) throws Exception {
		debug("generic fetch " + query );
		if ( !rep.isOpen() ) {
			rep.newTransaction();
		}
		Set<UserTableEntity> result = new LinkedHashSet<UserTableEntity>();
		for ( Object obj : rep.genericFetchList(query) ) {
			UserTableEntity ut = new UserTableEntity( (Map)obj );
			result.add(ut);
		}
		rep.closeSession();
		return result;
	}	
	
	public int getCount( String tableName, String criteria ) throws Exception {
		if ( !rep.isOpen() ) {
			rep.newTransaction();
		}
		return rep.getCount( tableName, criteria );
	}	
	
	public List<Relation> getList() throws NotFoundException {
		return rep.getList();	
	}	
	
	private void debug( String s ) {
		Logger.getInstance().debug(this.getClass().getName(), s );
	}	

	public Set<UserTableEntity> getTableStructure(String tableName) throws Exception {
		debug("get schema from table " + tableName );
		if ( !rep.isOpen() ) {
			rep.newTransaction();
		}
		return genericFetchList("SELECT column_name, data_type FROM information_schema.columns WHERE "
				+ "table_schema <> 'information_schema' and "
				+ "table_name='"+tableName+"'");
	}
	
	public void executeQueryAndKeepOpen(String query) throws Exception {
		if ( !rep.isOpen() ) {
			newTransaction();
		}
		rep.executeQueryAndKeepOpen(query);
	}
	
	public void commitAndClose() throws Exception {
		try {
			rep.commit();
			rep.closeSession();
		} catch ( Exception e ) {
			rep.rollBack();
			rep.closeSession();
			throw e;
		}
	}

	public void rollbackAndClose() throws Exception {
		try {
			rep.rollBack();
			rep.closeSession();
		} catch ( Exception e ) {
			throw e;
		}
	}	
	
}
