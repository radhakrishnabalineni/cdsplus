package com.hp.cdsplus.utils;
/**
 * 
 */


/**
 * @author kashyaks
 *
 */
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
public class TimeStampWriter extends PrintStream {

  private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss");

  /**
   * Constructor for TimeStampWriter.
   * @param os OutputStream
   */
  public TimeStampWriter(OutputStream os) {
    super(os);
  }

  /**
   * Method println.
   * @param s String
   */
  public void println(String s) {
    synchronized (dateFormat ) {
      super.print(dateFormat.format( new Date() ) );
      super.print(" ");
      super.println(s);
    }
  }
  
  /**
   * Method println.
   * @param o Object
   */
  public void println(Object o) {
    synchronized (dateFormat ) {
      super.print(dateFormat.format( new Date() ) );
      super.print(" ");
      super.println(o);
    }
  }  
}
