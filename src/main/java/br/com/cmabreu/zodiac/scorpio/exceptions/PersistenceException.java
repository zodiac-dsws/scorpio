package br.com.cmabreu.zodiac.scorpio.exceptions;

public class PersistenceException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public PersistenceException() {}
	public PersistenceException(String message){
		super(message);
	}
	
}
