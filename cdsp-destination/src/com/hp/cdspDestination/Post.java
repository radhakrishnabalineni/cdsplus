package com.hp.cdspDestination;

import java.io.IOException;

import com.hp.loader.priorityLoader.ProcessingException;




public class Post extends HttpMethod {
  
  public Post() {
    this( EMPTY_STRING );
  }

  public Post( final String baseUrl ) {
    super( baseUrl );
  }

  public byte[] resolve( final String path ) throws IOException, ProcessingException {
	  //create an empty byte array
	  byte empty[] = {};
	  return resolve( path, null, empty );
  }
  
  public byte[] resolve( final String path, final String mimeType, byte[] payload) throws IOException, ProcessingException {
  	return this.resolve(path, mimeType, payload, null, null);  
  }
  
  public byte[] resolve( final String path, final String mimeType, byte[] payload,  String eventType, Integer priority ) throws IOException, ProcessingException {
	  	return request(path, mimeType, payload, POST_METHOD_TEXT, eventType, priority );  
	  }
}
