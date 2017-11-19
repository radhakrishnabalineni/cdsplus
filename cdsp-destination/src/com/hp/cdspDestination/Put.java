package com.hp.cdspDestination;

import java.io.IOException;

import com.hp.loader.priorityLoader.ProcessingException;

public class Put extends HttpMethod {

	public Put() {
		super( EMPTY_STRING );
	}

	public Put( final String baseUrl ) {
		super( baseUrl );
	}

	public byte[] resolve( final String path ) throws IOException, ProcessingException {
		return this.resolve( path, null, (byte[])null );
	}

	public byte[] resolve( final String path, final String mimeType, byte[] payload ) throws IOException, ProcessingException {  
		// Create parameter list
		return this.resolve(path, mimeType, payload, null,null);
	}
	
	public byte[] resolve( final String path, final String mimeType, byte[] payload, String updateType, Integer priority ) throws IOException, ProcessingException {  
		// Create parameter list
		return request(path, mimeType, payload, PUT_METHOD_TEXT, updateType, priority);
		//Object[] parameters = {path, mimeType, payload, priority, updatetype};
	}

	/**
	 * 
	 * @param path
	 * @param mimeType
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @throws ProcessingException 
	 */
	public byte[] resolve( final String path, final String mimeType,
			String fileName) throws IOException, ProcessingException {
		return this.resolve(path, mimeType, fileName, null, null);
	}
	
	public byte[] resolve( final String path, final String mimeType,
			String fileName, String updateType, Integer priority) throws IOException, ProcessingException {
		return request(path, mimeType, fileName, PUT_METHOD_TEXT, updateType, priority);
	}

	public static void main( String[] args ) throws IOException, ProcessingException {
		byte[] outBytes = null;
		
		//set 1
		
		//Put put = new Put( "http://localhost:7001/cadence/app/manual/content/c50284541/");
		//Put put = new Put( "http://g1u0971c.austin.hp.com:30052/cadence/app/");
		//Put put = new Put( "http://cdsplus-itg.austin.hp.com/cadence/app/");
		Put put = new Put( "http://localhost:7001/cadence/app/");
		outBytes = put.resolve( "supportcontent/loader/c50303862/", "text/xml" , inputFile( "C:/Documentum/Checkout/c50303862.xml" ), "update1", 1 );
		
		//Put put = new Put( "http://localhost:7001/cadence/app/manual/content/c50284541/4AA1-2174ENW.pdf");
		//outBytes = put.resolve( "library/loader/c50284541", "text/xml" , inputFile( "C:/12.2-AY/Test-Data- concentra loader/c50284541.xml" ) );
		
		outputFile( "C:/opt/sasuapps/cdsplus/logs/cadence/bytes.txt", outBytes );
		System.out.println("12.4 ....done"); 
		
		/*
		final String usage_message = "java Put <URL> [<Input File> <Mime Type>] [<Output File> [<key/trust store> <store password>]]";

		if ( args.length == 0 || args.length > 6 ) {
			System.out.println( "Incorrect number of parameters: " + args.length );
			System.out.println( usage_message );
			System.exit( 1 );
		}
		Put put = new Put( args[ 0 ] );
		byte[] outBytes = null;
		switch ( args.length ) {
		case 1: // <URL>
			put.resolve( "" );
			break;
		case 2: // <URL> <Output File>
			outBytes = put.resolve( "" );
			outputFile( args[ 1 ], outBytes );
			break;
		case 3: // <URL> <Input File> <Mime Type>
			put.resolve( "", args[ 2 ], inputFile( args[ 1 ] ) );
			break;
		case 4: // <URL> <Input File> <Mime Type> <OutputFile>
			outBytes = put.resolve( "", args[ 2 ], inputFile( args[ 1 ] ) );
			outputFile( args[ 3 ], outBytes );
			break;
		case 5:
			System.out.println( "Incorrect number of parameters: " + args.length );
			System.out.println( usage_message );
			System.exit( 1 );
		case 6: // <URL> <Input File> <Mime Type> <OutputFile> <key/trust store> <storepasswd>
			System.setProperty( "javax.net.ssl.keyStore", args[ 4 ] );
			System.setProperty( "javax.net.ssl.trustStore", args[ 4 ] );
			System.setProperty( "javax.net.ssl.keyStoreType", "jks" ); 
			System.setProperty( "javax.net.ssl.trustStoreType", "jks" ); 
			System.setProperty( "javax.net.ssl.keyStorePassword", args[ 5 ] );
			System.setProperty( "javax.net.ssl.trustStorePassword", args[ 5 ] ); 
			outBytes = put.resolve( "", args[ 2 ], inputFile( args[ 1 ] ) );
			outputFile( args[ 3 ], outBytes );
		}*/
	}
}
