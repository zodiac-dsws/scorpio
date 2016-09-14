package br.com.cmabreu.zodiac.scorpio.services;

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


}
