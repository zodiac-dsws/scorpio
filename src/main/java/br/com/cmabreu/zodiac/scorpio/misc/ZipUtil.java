package br.com.cmabreu.zodiac.scorpio.misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.DatatypeConverter;

public class ZipUtil {
	
	public static String toHexString( byte[] array ) {
	    return DatatypeConverter.printHexBinary(array);
	}
	
	public static byte[] toByteArray( String s ) {
	    return DatatypeConverter.parseHexBinary(s);
	}	
	
	public static byte[] compress(String string) {
		byte[] compressed = null;
		try {
		    ByteArrayOutputStream os = new ByteArrayOutputStream(string.length());
		    GZIPOutputStream gos = new GZIPOutputStream(os);
		    gos.write(string.getBytes());
		    gos.close();
		    compressed = os.toByteArray();
		    os.close();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	    return compressed;
	}

	public static String decompress(byte[] compressed) {
		StringBuilder string = new StringBuilder();
		try {
		    final int BUFFER_SIZE = 32;
		    ByteArrayInputStream is = new ByteArrayInputStream(compressed);
		    GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
		    byte[] data = new byte[BUFFER_SIZE];
		    int bytesRead;
		    while ((bytesRead = gis.read(data)) != -1) {
		        string.append(new String(data, 0, bytesRead));
		    }
		    gis.close();
		    is.close();
		} catch ( Exception e) {
			e.printStackTrace();
		}
	    return string.toString();
	}
	
	
	public static void compressFile(String source_filepath, String destinaton_zip_filepath) throws Exception {
		byte[] buffer = new byte[1024];
		FileOutputStream fileOutputStream =new FileOutputStream(destinaton_zip_filepath);
		GZIPOutputStream gzipOuputStream = new GZIPOutputStream(fileOutputStream);
		FileInputStream fileInput = new FileInputStream(source_filepath);
		int bytes_read;
		while ((bytes_read = fileInput.read(buffer)) > 0) {
			gzipOuputStream.write(buffer, 0, bytes_read);
		}
		fileInput.close();
		gzipOuputStream.finish();
		gzipOuputStream.close();
		fileOutputStream.close();
	}	
	
	
	public static void decompressFile( String compressedFile, String decompressedFile ) throws Exception {
		byte[] buffer = new byte[1024];
		try {
			FileInputStream fileIn = new FileInputStream(compressedFile);
			GZIPInputStream gZIPInputStream = new GZIPInputStream(fileIn);
			FileOutputStream fileOutputStream = new FileOutputStream(decompressedFile);
			int bytes_read;
			while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {
				fileOutputStream.write(buffer, 0, bytes_read);
			}
			gZIPInputStream.close();
			fileOutputStream.close();
		} catch (IOException ex) {
			throw new Exception( ex.getMessage() );
		}
	}		
}
