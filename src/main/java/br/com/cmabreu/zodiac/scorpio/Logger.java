package br.com.cmabreu.zodiac.scorpio;

public class Logger {
	private static Logger instance;
	private boolean enabled;
	
	public void enable() {
		enabled = false;
	}
	
	public void disable() {
		enabled = false;
	}
	
	public static Logger getInstance() {
		if ( instance == null ) {
			instance = new Logger();
		}
		return instance;
	}
	
	public void debug( String className, String message ) {
		if ( !enabled ) { return; }
	}
	
	public void error( String className, String message ) {
		if ( !enabled ) { return; }
	}
	
	public void warn( String className, String message ) {
		if ( !enabled ) { return; }
	}
	

}
