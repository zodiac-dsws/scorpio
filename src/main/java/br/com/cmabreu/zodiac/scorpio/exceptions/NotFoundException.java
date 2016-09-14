package br.com.cmabreu.zodiac.scorpio.exceptions;

public class NotFoundException extends PersistenceException {
	private static final long serialVersionUID = 1L;

	public NotFoundException(String message){
		super(message);
	}
	
}
