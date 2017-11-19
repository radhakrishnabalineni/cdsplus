package com.hp.soar.priorityLoader.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 
 * Class used to drain the input from a stream and store it in a buffer
 * due to the limited buffer size running Unix command from JVM.
 * A new daemon thread is spawned while the scanning is running
 */
public class StreamConsumer implements Runnable {
  private final static int BUFSIZE = 1024;
  private BufferedReader  reader;
  private int bytesRead = 0;
  private char[] inBuf = new char[BUFSIZE];
  StringBuffer answer = new StringBuffer();

/**
 *  
 * @param s: output stream from the scanner 
 * @param tName: type of output stream from the scanner
 */
  public StreamConsumer(InputStream s, String tName) {
    reader = new BufferedReader(new InputStreamReader(s));
    Thread t = new Thread(this, tName);
    t.setDaemon(true);
    t.start();
  }

  /**
   * Close the stream
   * @throws IOException
   */
  public void close() throws IOException {
    reader.close();
  }

  /**
   * Return the string represention of the buffer content
   * @return: String
   */
  public String getResult() {
    return answer.toString();
  }

  /**
   * Spawn a new thread to read in the stream and append to the string buffer
   */
  public void run() {
    try {
      //        System.err.println(Thread.currentThread().getName()+" Begin Reading");
      while ((bytesRead = reader.read(inBuf, 0, BUFSIZE)) != -1) {
        //          System.err.println(Thread.currentThread().getName()+" Read "+inRead+" bytes");
        answer.append(inBuf, 0, bytesRead);
      }
    } catch (IOException ioe) {
      System.err.println(Thread.currentThread().getName()+":"+ ioe.getMessage());
      ioe.printStackTrace();
    }
  }
}


