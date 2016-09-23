package br.com.cmabreu.zodiac.scorpio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import br.com.cmabreu.zodiac.federation.objects.CoreObject;
import br.com.cmabreu.zodiac.scorpio.config.Configurator;
import br.com.cmabreu.zodiac.scorpio.misc.Activation;
import br.com.cmabreu.zodiac.scorpio.misc.FileUnity;
import br.com.cmabreu.zodiac.scorpio.misc.ZipUtil;
import br.com.cmabreu.zodiac.scorpio.services.RelationService;
import br.com.cmabreu.zodiac.scorpio.storages.IStorage;
import br.com.cmabreu.zodiac.scorpio.storages.LocalStorage;
import br.com.cmabreu.zodiac.scorpio.types.ExecutorType;
import br.com.cmabreu.zodiac.scorpio.types.TaskStatus;

public class Task implements Runnable {
	private List<String> sourceData;
	private List<String> console;
	private int exitCode;
	private Date realStartTime;
	private Date realFinishTime;
	private int PID;
	private CoreObject owner;
	private Activation activation;
	private long startTimeMillis;
	private List<String> execLog = new ArrayList<String>();	
	private TaskStatus status = TaskStatus.STOPPED;
	private IStorage storage;

	private void debug( String s ) {
		if ( !s.equals("")) {
			execLog.add( s );
		}
	}
	
	private void error( String s ) {
		if ( !s.equals("")) {
			execLog.add( s );
		}
	}		
	
	public List<String> getExecLog() {
		return execLog;
	}
	
	public int getPID() {
		return PID;
	}

	public Date getRealFinishTime() {
		return realFinishTime;
	}

	public Date getRealStartTime() {
		return realStartTime;
	}

	public void setRealFinishTime(Date realFinishTime) {
		this.realFinishTime = realFinishTime;
	}

	public void setRealStartTime(Date realStartTime) {
		this.realStartTime = realStartTime;
	}

	public List<String> getSourceData() {
		return sourceData;
	}

	public List<String> getConsole() {
		return console;
	}

	public void setSourceData(List<String> sourceData) {
		this.sourceData = sourceData;
	}


	public Task( CoreObject owner ) {
		this.console = new ArrayList<String>();
		this.owner = owner;
	}

	
	private void dump() {
		try {
			String fileName = "dump/" + owner.getSerial() + "/";
			File fil = new File( "dump" );
			fil.mkdirs();
			
			FileWriter fw = new FileWriter(fileName, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter writer = new PrintWriter(bw);
		    
			writer.println("-------------------------------------------------------------------");			
			writer.println("Workflow           : " + activation.getWorkflow() );
			writer.println("Experiment         : " + activation.getExperiment() );
			writer.println("Fragment           : " + activation.getFragment() );
			writer.println("Activity           : " + activation.getActivitySerial() );
			writer.println("Instance           : " + activation.getInstanceSerial() );
			writer.println("Command            : " + activation.getCommand() );
			writer.println("Executor           : " + activation.getExecutor() );
			writer.println("Executor Type      : " + activation.getExecutorType() );
			writer.println("Target Table       : " + activation.getTargetTable() );
			writer.println("Start Time         : " + activation.getStartTime() );
			writer.println("End Time           : " + activation.getEndTime() );
			writer.println("-------------------------------------------------------------------");			
			writer.println( activation.getXmlOriginalData() );
			writer.println("-------------------------------------------------------------------");			
			writer.println("");
			writer.println("");
		    
			writer.println("----------------------- RESULT ------------------------------------");
			for ( String s : getExecLog() ) {
				writer.println("     " + s );
			}
			writer.println(""); writer.println("");
			for ( String s : getConsole() ) {
				writer.println("     " + s );
			}
			writer.println("-------------------------------------------------------------------");			
			
			
			writer.close();
		    
		} catch ( Exception e ) {
			System.out.println("Cannot Dump Core " + owner.getSerial() );
		}
		
	}
	
	public TaskStatus getTaskStatus() {
		return this.status;
	}	
	
	
	private void runTask() {
		Process process = null;
		status = TaskStatus.RUNNING;
		try {
			debug("running external wrapper " + activation.getCommand() );

			process = Runtime.getRuntime().exec( activation.getCommand() );
			
			/*
        	List<String> args = new ArrayList<String>();
        	args.add("/bin/sh");
        	args.add("-c");
        	args.add( activation.getCommand() );			
        	process = new ProcessBuilder( args ).start();
        	*/
			
			/*
			PID = 0;
			if( process.getClass().getName().equals("java.lang.UNIXProcess") ) {
				try {
					Field f = process.getClass().getDeclaredField("pid");
					f.setAccessible(true);
					PID = f.getInt( process );
				} catch (Throwable e) {

				}
			}
			*/

			InputStream in = process.getInputStream(); 
			BufferedReader br = new BufferedReader( new InputStreamReader(in) );
			String line = null;
			
			InputStream es = process.getErrorStream();
			BufferedReader errorReader = new BufferedReader(  new InputStreamReader(es) );
			while ( (line = errorReader.readLine() ) != null) {
				console.add( line );
				error( line );
			}	
			errorReader.close();

			while( ( line=br.readLine() )!=null ) {
				console.add( line );
				debug( "[" + activation.getActivitySerial() + "] " + activation.getExecutor() + " > " + line );
			}  
			br.close();

			exitCode = process.waitFor();


		} catch ( Exception ex ){
			error( ex.getMessage() );
			for ( StackTraceElement ste : ex.getStackTrace() ) {
				error( ste.toString() );
			}
		}
		status = TaskStatus.FINISHED;
		debug("external wrapper finished.");
	}
	
	
	/**
	 * BLOCKING
	 * Will execute a external program (wrapper)
	 * WIll block until task is finished
	 * 
	 */
	public void executeCommand( String command ) throws Exception {
		saveInputData( activation );
		saveXmlData( activation );		
		activation.setCommand( generateCommand( activation ) );
		runTask();
	}

	/**
	 * BLOCKING
	 * Will execute a external program (wrapper)
	 * WIll block until task is finished
	 * 
	 */
	public void executeSqlCommand( String command ) throws Exception {
		try {
			RelationService rs = new RelationService();
			rs.executeQuery( command );
			debug( command );
		} catch ( Exception e ) {
			for ( StackTraceElement ste : e.getStackTrace() ) {
				execLog.add( e.getMessage() );
				execLog.add( ste.getClassName() + " " + ste.getLineNumber() + ": " + ste.getMethodName()  );
			}
			
		}
	}
	
	private void createWorkFolder( Activation act ) throws Exception {
		File outputFolder = new File( act.getNamespace() + "/" + "outbox" );
		outputFolder.mkdirs();

		File inputFolder = new File( act.getNamespace() + "/" + "inbox" );
		inputFolder.mkdirs();
	}
	
	private String copyToWorkDir ( FileUnity file, Activation act ) throws Exception {
		
		File temp = new File( file.getName() );
		String fileName = temp.getName();
		String target = act.getNamespace() + "/" + "inbox" + "/" + fileName;
		debug("providing file " + file.getName() + " for " + act.getTaskId() + " (" + act.getExecutor() + ")");
		
		boolean result = storage.copyToLocalFS( act, file, target );
		if ( !result ) {
			error("could not copy the file " + file.getName() );
			throw new Exception( "could not copy the file " + file.getName() );
		}
		
		return fileName;
	}
	
	private void saveXmlData( Activation act ) throws Exception {
		FileWriter writer = new FileWriter( act.getNamespace() + "/" + "sagi_source_data.xml"); 
		String xml = act.getXmlOriginalData();
		xml = xml.replaceAll("><", ">\n<");
		writer.write( xml );
		writer.close();		
		debug("XML source data file saved");
	}
	
	private void saveInputData( Activation act ) throws Exception {
		debug("start data preparation for task " + act.getExecutor() + " (Activity: " + act.getActivitySerial() + "/ Task: " + act.getTaskId() + ")" );
		if ( act.getSourceData().size() < 2 ) {
			// We need at least 2 lines ( one line for header and one line of data )
			error( "Not enough input data. Aborting..." );
			throw new Exception ("Not enough data in input CSV for Task " + act.getActivitySerial() );
		}
		createWorkFolder(act);
		
		Activation previous = act.getPreviousActivation(); 
		if ( previous != null ) {
			// So this is not the first task inside instance. 
			String previousOutbox = previous.getNamespace() + "/" + "outbox";
			String destInbox = act.getNamespace() + "/" + "inbox";

			// Copy sagi_output.txt from previous task to this task source data.
			act.setSourceData( readFile( previous.getNamespace() + "/" + "sagi_output.txt" ) );

			// Save previous output as this input
			FileWriter writer = new FileWriter( act.getNamespace() + "/" + "sagi_input.txt"); 
			for(String str: act.getSourceData() ) {
			  writer.write( str + "\n" );
			}
			writer.close();
			debug( "input data file sagi_input.txt saved with " + act.getSourceData().size() + " lines");
			
			// Copy files from previous task's output to this input box.
			File source = new File( previousOutbox );
			File dest = new File( destInbox );
			if ( !isDirEmpty( source.toPath() )  ) {
				debug(" > will copy files from previous task " + previous.getExecutor() + " ("+ previous.getTaskId() + ") ..." );
				debug("   from > " + previousOutbox );
				debug("   to   > " + destInbox );
				
				FileUtils.copyDirectory( source, dest );
			}
		} else {
			// This is the first task in instance
			FileWriter writer = new FileWriter( act.getNamespace() + "/" + "sagi_input.txt"); 
			for(String str: act.getSourceData() ) {
			  writer.write( str + "\n" );
			}
			writer.close();
			debug( "input data file sagi_input.txt saved with " + act.getSourceData().size() + " lines");
			
			// Check if Sagitarii ask us to download some files...
			if ( act.getFiles().size() > 0 ) {
				debug("this task needs to download " + act.getFiles().size() + " files: ");
				
				for ( FileUnity file : act.getFiles() ) {
					debug("need file " + file.getName() + " (id " + file.getId() + ") for attribute " + file.getAttribute() +
							" of table " + file.getSourceTable() );
					

					String justFileName = copyToWorkDir(file, act);
					String targetName = act.getNamespace() + "/" + "inbox" + "/" + justFileName;
					
					ZipUtil.decompressFile( targetName + ".gz", targetName );
					
					File fil = new File( targetName );
					if ( fil.exists() ) {
						debug( "file " + targetName + " downloaded.");
					} else {
						error( "cannot find file " + targetName + " after download.");
					}
					
				}
			} else {
				debug("no need to download files.");
			}
	
		}
		debug("done preparing task " + act.getExecutor() + " (" + act.getActivitySerial() + "/" + act.getTaskId() + ")" );
	}
	
	private boolean isDirEmpty( Path directory ) throws IOException {
		boolean result = false;
	    try( DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory) ) {
	        result = !dirStream.iterator().hasNext();
	        dirStream.close();
	    }
	    return result;
	}
	
	
	private List<String> readFile( String file ) throws Exception {
		String line = "";
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader br = new BufferedReader( new FileReader( file ) );
		while ( (line = br.readLine() ) != null ) {
		    list.add( line );
		}
		if (br != null) {
			br.close();
		}		
		return list;
	}
	
	
	private String generateCommand( Activation activation ) throws Exception {
		String command = activation.getCommand();
		
		String classPathParam = ""; // <----  -Xmx -Xms etc ... 
		
		String workFolder = activation.getNamespace();
		String wrappersFolder = Configurator.getInstance().getWrappersFolder();
		
		if ( activation.getExecutorType().equals("RSCRIPT") ) {
			String wrapperCommand = wrappersFolder + "r-wrapper.jar";
			String scriptFile = wrappersFolder + activation.getCommand();
			command = "java "+classPathParam+" -jar "+ wrapperCommand + " " + scriptFile + " " + workFolder + " " + wrappersFolder;
			
		} else if ( activation.getExecutorType().equals("BASH") ) {
			command = "bash "   + wrappersFolder + activation.getCommand() + " " + workFolder + " " + wrappersFolder;
		} else if ( activation.getExecutorType().equals("PYTHON") ) {
			command = "python " + wrappersFolder + activation.getCommand() + " " + workFolder + " " + wrappersFolder;
		} else {
			command = "java "+classPathParam+" -jar " + wrappersFolder + activation.getCommand() + " " + workFolder + " " + wrappersFolder;
		}
		return command;
	}	
	
	public int getExitCode() {
		return this.exitCode;
	}

	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}
	
	public void setActivation( Activation act ) {
		this.activation = act;
	}
	
	public Activation getActivation() {
		return activation;
	}

	public long getTimeMillis() { 
		long estimatedTime = ( Calendar.getInstance().getTimeInMillis() - startTimeMillis );
		return estimatedTime;
	}
	
	public String getTime(  ) {
		long millis = getTimeMillis();
		String time = String.format("%03d %02d:%02d:%02d", 
				TimeUnit.MILLISECONDS.toDays( millis ),
				TimeUnit.MILLISECONDS.toHours( millis ),
				TimeUnit.MILLISECONDS.toMinutes( millis ) -  
				TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours( millis ) ), 
				TimeUnit.MILLISECONDS.toSeconds( millis ) - 
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes( millis ) ) );
		return time;
	}	

	
	@Override
	public void run() {
		int exitCode = 0;
		startTimeMillis = Calendar.getInstance().getTimeInMillis();
		activation.setStartTime( Calendar.getInstance().getTime() );
		
		storage = new LocalStorage();
		
		try {

			if ( activation.getExecutorType() == ExecutorType.SELECT ) {
				executeSqlCommand( activation.getCommand() );
			} else {
				executeCommand( activation.getCommand() );
			}
			
		} catch ( Exception e ) {
			exitCode = 1;
		}
		
		storeData();		
		activation.setEndTime( Calendar.getInstance().getTime() );
		
		dump();
		owner.notifyFinishedByTask( exitCode );
	}

	private void sanitize( ) throws Exception {
		
		debug("deleting work folder");
		FileUtils.deleteDirectory( new File( getActivation().getNamespace() ) ); 
		
		
		/*
		if ( Configurator.getInstance().getClearDataAfterFinish() ) {
			debug("deleting work folder");
			FileUtils.deleteDirectory( new File( getActivation().getNamespace() ) ); 
		}
		*/
	}		
	
	private void importCsvFile() throws Exception {
		String taskFolder = activation.getNamespace();
		String csvFileName = taskFolder + "/" + "sagi_output.txt";
		String outbox = taskFolder + "/" + "outbox";
		
		DomainStorage ds = DomainStorage.getInstance();
		
		File csvFile = new File( csvFileName );
		
		CSVFormat format = CSVFormat.RFC4180.withDelimiter( Configurator.getInstance().getCSVDelimiter() );
		CSVParser parser = CSVParser.parse(csvFile, Charset.defaultCharset(), format );
		CSVRecord headerLine = null;	
		List<String> contentLines = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		String prefix = "";
		boolean headerReady = false;
		
		String targetTable = activation.getTargetTable();
		
		for (CSVRecord csvRecord : parser) {
			if ( headerLine == null ) { 
				headerLine = csvRecord; // Get the CSV header line.
			}
			
			// For each line, parsing columns...
			for ( int x = 0; x < csvRecord.size(); x++ ) {
				// Mount the columns line (line 0)
				if ( !headerReady ) {
					String columnName = csvRecord.get(x).replace("'", "`");
					sb.append( prefix + columnName );
					prefix = ",";
					continue;
				}
				
				// Check if any field of csv is a reference to a file
				// Get the clumn name ( from CSV header line )
				String columnName = headerLine.get(x); 							
				// Get the column content ( the data )
				String columnContent = csvRecord.get(x).replace("'", "`");
				String domainName =  targetTable + "." + columnName;
				
				if ( ds.domainExists( domainName ) ) {
					
					// This column is a file. The file name is in columnContent.
					// Just add the full target path to the file name before store it.
					String targetPath = activation.getWorkflow() + "/" + 
							activation.getExperiment() + "/" + activation.getActivitySerial() + "/" + activation.getTaskId();
					
					String localFile = outbox + "/" + columnContent;
					columnContent = targetPath + "/" + columnContent;
					
					storage.copyToRemoteFS( localFile, columnContent );
					
				}
				sb.append( prefix + columnContent );
				prefix = ",";				
			}
			
			headerReady = true;
			contentLines.add( sb.toString() );
			sb.setLength(0);
			prefix = "";			
		}
		
		parser.close();
		if ( contentLines.size() > 1 ) {
			debug("Inserting " + contentLines.size() + " lines in table " + targetTable + "...");
			importCSVData( contentLines );
			debug("Done saving data to table " + targetTable );
		}
		
	}

	private void storeData() {
		String actId = activation.getInstanceSerial() + ":" + activation.getExecutor();
		debug("Storing Activity " + actId + " data...");

		
		if ( !validateProduct( ) ) {
			error( "Activity " + actId + " did not produce the CSV output file. No data to store." );
		} else {
			debug("product is valid.");
			try {
				importCsvFile( );
			} catch ( Exception e ) {
				error( e.getMessage() );
			}
		}		
		
		try {
			sanitize();
		} catch ( Exception e ) {
			error( e.getMessage() );
		}
	}	
	
	
	private boolean validateProduct() {
		String taskFolder = activation.getNamespace();
		String sagiOutput = taskFolder + "/" + "sagi_output.txt";
		String outbox = taskFolder + "/" + "outbox";
		String actId = activation.getInstanceSerial() + ":" + activation.getExecutor();
		
		try {
			File file = new File(sagiOutput);
			if( !file.exists() ) { 
				debug( actId + ": output CSV data file " + sagiOutput + " not found");
				return false;
			} 
			if ( file.length() == 0 ) { 
				debug( actId + ": output CSV data file " + sagiOutput + " is empty");
				return false;
			} else {
				debug( actId + ": sagi_output.txt have " + file.length() + " bytes.");
			}
			BufferedReader br = new BufferedReader( new FileReader( file ) );
			String header = br.readLine(); 					
			if ( header == null ) { 
				debug( actId + ": output CSV data file " + sagiOutput + " have no header line");
				br.close();
				return false;
			} 
			String line = br.readLine(); 					
			if ( line == null ) { 
				debug( actId + ": output CSV data file " + sagiOutput + " have no data line");
				br.close();
				return false;
			} 
			br.close();
		} catch ( Exception e ) {
			debug( "validation error: " + actId + ": " + e.getMessage() );
			return false;
		}
		
		File outboxDir = new File( outbox );
		if( outboxDir.list().length == 0 ){
			debug("no files found in outbox");
		} else {
			debug( outboxDir.list().length + " files found in outbox (first 10):");
			int limit = 10;
			if ( limit > outboxDir.list().length ) {
				limit = outboxDir.list().length;
			}
			for ( int i = 0; i < limit; i++  ) {
				debug( " > " + outboxDir.list()[i] );
			}
		}
		outboxDir = null;
		return true;
	}

	
	private void importCSVData( List<String> contentLines ) throws Exception {
		String sql = "";
		RelationService rs = new RelationService();
		UserTableEntity triad = rs.getTriad( activation.getInstanceSerial() );
		
		int idInstance = Integer.valueOf( triad.getData("id_instance") );
		int idExperiment = Integer.valueOf( triad.getData("id_experiment") );
		int idActivity = Integer.valueOf( triad.getData("id_activity") ); 
		
		
		String targetTable = activation.getTargetTable();
		rs.newTransaction();
		Set<UserTableEntity> structure = rs.getTableStructure( targetTable );
		
		debug("Will insert " + contentLines.size() + " lines of data. Instance " + activation.getInstanceSerial() + " | Activity " + activation.getActivitySerial() );
		for ( int x = 1; x < contentLines.size(); x++ ) {
			
			String ss = contentLines.get(x);
			if ( (ss == null) || (ss.equals("" )) ) {
				continue;
			}
			
			String values = "";
			String columns = "id_experiment,id_activity,id_instance," ;
			
			char delimiter = Configurator.getInstance().getCSVDelimiter();
			String emptyData = delimiter + "" + delimiter;
			String nullData = delimiter + "null" + delimiter;

			// Is the middle value null?
			String contentDataLine = contentLines.get(x).replace(emptyData, nullData);
			
			// Is the last value null? 
			if ( contentDataLine.endsWith( String.valueOf( delimiter ) ) ) {
				contentDataLine = contentDataLine + "null";
			}
			// Is the first value null? 
			if ( contentDataLine.startsWith( String.valueOf( delimiter ) ) ) {
				contentDataLine = "null" + contentDataLine;
			}
			// =============================
			
			String[] columnsArray = contentLines.get(0).split( String.valueOf( Configurator.getInstance().getCSVDelimiter() ) );
			String[] valuesArray = contentDataLine.split( String.valueOf( delimiter ) );
			
			for ( int z = 0; z < columnsArray.length; z++ ) {
				try {
					if ( attributeExists( columnsArray[z], structure ) ) {
						columns = columns + columnsArray[z] + ",";
						if ( valuesArray[z].equals("null") ) {
							values = values + valuesArray[z] + ",";
						} else {
							values = values + "'" + valuesArray[z] + "',";
						}
					}
				} catch ( Exception ex ) {
					error("Error when inserting data into table " + targetTable );
					ex.printStackTrace();
				}
			}
			if ( values.trim().length() > 0 ) {
				values = values.substring(0, values.length()-1);
			} else {
				throw new Exception("None of your CSV columns match table '" + targetTable + "' attribute list. Is this the right table?");
			}

			sql = "insert into " + targetTable + "("+columns.substring(0, columns.length()-1).toLowerCase()+") values ("+ idExperiment + 
					"," + idActivity + "," + idInstance + "," + values + ");";
			
			debug(" > " + sql );
			
			try {
				rs.executeQueryAndKeepOpen(sql);
			} catch ( Exception e ) {
				error( e.getMessage() );
				rs.rollbackAndClose();
				throw e;
			}
			
		}
		rs.commitAndClose();
	}
	
	private boolean attributeExists( String attribute, Set<UserTableEntity> structure ) {
		for ( UserTableEntity ute : structure  ) {
			if ( ute.hasContent( attribute ) ) {
				return true;
			}
		}
		return false;
	}
	


	
}