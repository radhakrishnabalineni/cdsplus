package com.hp.cdspDestination;

import java.io.IOException;

import com.hp.loader.priorityLoader.ProcessingException;

public class Get extends HttpMethod {

	public Get() {
		this( EMPTY_STRING );
	}

	public Get( final String baseUrl ) {
		super( baseUrl );
	}

	public byte[] resolve( final String path ) throws IOException, ProcessingException {   
		return request(path, null, (byte[])null, GET_METHOD_TEXT, null, null);  
	}

	public byte[] resolve( final String path, final String mimeType, byte[] payload ) throws IOException, ProcessingException {
		return resolve( path );
	}

	public static void main( String[] args ) throws IOException, ProcessingException {
		final String usage_message = "java Get <URL> [<Output File>]";

		Runtime rt = Runtime.getRuntime();
		long startMem = rt.totalMemory() - rt.freeMemory();
		long startTime = System.currentTimeMillis();
		if ( !(args.length == 1 || args.length == 2) ) {
			System.out.println( "Incorrect number of parameters: " + args.length );
			System.out.println( usage_message );
			System.exit( 1 );
		}
		Get get = new Get( args[ 0 ] );
		if ( args.length == 2 ) outputFile( args[ 1 ], get.resolve( "" ) );
		long elapseTime = System.currentTimeMillis() - startTime;
		long deltaMem = rt.totalMemory() - rt.freeMemory() - startMem;
		System.out.println( "time (ms): " + elapseTime );
		System.out.println( "mem (bytes): " + deltaMem );
	}
}
