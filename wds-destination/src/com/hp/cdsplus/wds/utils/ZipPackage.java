package com.hp.cdsplus.wds.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.NDC;

public class ZipPackage {

  private String finalFilename;
  private File finalFile;
  private File tempFile;
  private ZipOutputStream zos;
  private int count = 0;
  private long size = 0;
  private boolean closed = false;
  private boolean ndcIsSet = false;

  public ZipPackage( File finalFile, File tempFile ) throws IOException {
    this.tempFile = tempFile;
    this.finalFile = finalFile;
    finalFilename = finalFile.getName();
    zos = new ZipOutputStream( new BufferedOutputStream( new FileOutputStream( this.tempFile ) ) );
  }

  public void movePackage() throws IOException {
    tempFile.renameTo( finalFile );
  }

  public String getFinalFilename() {
    return finalFilename;
  }
  
  public String getFinalDirectory() {
    return finalFile.getParentFile().getAbsolutePath();
  }
  
  public void setNDCLogging() {
    if (!ndcIsSet) NDC.push(getFinalFilename());
    ndcIsSet = true;
  }
  
  public void removeNDCLogging() {
    if (ndcIsSet) NDC.pop();
    ndcIsSet = false;
  }

  public ZipPackage closePackage() throws IOException {
    ZipPackage zp = null;
    //ensuring it is safe to call multiple times for retry support
    if ( !closed ) {
      //just making' sure something is in the Zip file or it whines when closed
      if ( count <= 0 ) {
        ZipEntry ze = new ZipEntry("fakeentry");
        zos.putNextEntry( ze );
        zos.closeEntry();
        zos.close();
        tempFile.delete();
      } else {
        zos.close();
        zp = this;
      }
      closed = true;
    }
    return zp;
  }

  public void putEntry( String zipPath, File contentTemp ) throws IOException {
    BufferedInputStream bis = null;
    try {
      ZipEntry ze = new ZipEntry( zipPath );
      zos.putNextEntry( ze );
      bis = new BufferedInputStream( new FileInputStream( contentTemp ) );
      byte[] chunk = new byte[ IDestinationConstants.IO_CHUNK_SIZE ];
      int pos = 0;
      while ( (pos = bis.read( chunk )) != -1 ) {
        zos.write( chunk, 0, pos );
      }
    } finally {
      if ( bis != null ) bis.close();
      zos.flush();
      zos.closeEntry();
    }
    size = tempFile.length();
    //size += contentTemp.length();
    count++;
  }

  public boolean shouldClose( int maxCount, long maxSize ) {
    return size > maxSize || count > maxCount; 
  }
}

