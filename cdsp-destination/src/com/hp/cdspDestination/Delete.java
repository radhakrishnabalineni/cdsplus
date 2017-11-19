package com.hp.cdspDestination;

import java.io.IOException;

import com.hp.loader.priorityLoader.ProcessingException;

public class Delete extends HttpMethod {

	public Delete() {
		this( EMPTY_STRING );
	}

	public Delete( final String baseUrl ) {
		super( baseUrl );
	}

	public Delete(String projUrl, boolean activeRetry) {
		this(projUrl);
	}

	public byte[] resolve( final String path, final String mimeType, byte[] payload ) throws IOException, ProcessingException {
		return this.resolve( path );
	}
	
	public byte[] resolve( final String path, final String mimeType, byte[] payload , String eventType, Integer priority) throws IOException, ProcessingException {
		return request(path, null, (byte[])null, DELETE_METHOD_TEXT, eventType, priority );
	}
	
	public byte[] resolve( final String path, String eventType, Integer priority) throws IOException, ProcessingException {
		return this.resolve(path, null, (byte[])null, eventType, priority );
	}

	public byte[] resolve( final String path ) throws IOException, ProcessingException {     	  
		return this.resolve(path, null, (byte[])null, null, null);
	}  

	public static void main( String[] args ) throws IOException, ProcessingException {
		byte[] outBytes = null;
		
		Delete del = new Delete( "http://localhost:7001/cadence/app/");
		outBytes = del.resolve( "soar/pdapi_201/c50284541/", "delete", 7 );
		
		System.out.println("12.2 ....done");
		
		
		/*final String usage_message = "java Delete <URL> [<Output File>]";

		if ( !(args.length == 1 || args.length == 2) ) {
			System.out.println( "Incorrect number of parameters: " + args.length );
			System.out.println( usage_message );
			System.exit( 1 );
		}
		Delete del = new Delete( args[ 0 ] );
		byte[] outBytes = del.resolve( "" );
		if ( args.length == 2 ) outputFile( args[ 1 ], outBytes );*/
	}
}
