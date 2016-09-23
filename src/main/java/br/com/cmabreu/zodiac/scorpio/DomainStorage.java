package br.com.cmabreu.zodiac.scorpio;

import java.util.ArrayList;
import java.util.List;

import br.com.cmabreu.zodiac.scorpio.entity.Domain;


public class DomainStorage {
	private static List<Domain> domains;
	private static DomainStorage instance;
	
	public static DomainStorage getInstance() {
		if ( instance == null ) {
			instance = new DomainStorage();
		}
		return instance;
	}
	
	public void addDomain( Domain domain ) {
		domains.add( domain );
	}
	
	private void debug( String s ) {
		Logger.getInstance().debug( this.getClass().getName(), s );
	}

	private DomainStorage() {
		domains = new ArrayList<Domain>();
	}
	
	public void setDomains(List<Domain> newDomains) {
		domains = newDomains;
	}

	public Domain getDomain( String domainName ) {
		
		if ( domains.size() == 0 ) {
			debug("No domains found.");
			return null;
		}
		
		for ( Domain domain : domains  ) {
			if ( domain.getDomainName().equals( domainName ) ) {
				return domain;
			}
		}
		return null;
	}

	public boolean domainExists( String domainName ) {
		
		if ( domains.size() == 0 ) {
			debug("No domains found.");
			return false;
		}
		
		for ( Domain domain : domains  ) {
			if ( domain.getDomainName().equals( domainName ) ) {
				return true;
			}
		}
		return false;
	}

	public boolean isColumnADomain( String columnName ) {
		
		if ( domains.size() == 0 ) {
			debug("No domains found.");
			return false;
		}
		
		for ( Domain domain : domains  ) {
			if ( domain.getDomainName().contains( "." + columnName ) ) {
				return true;
			}
		}
		return false;
	}
	
	
}
