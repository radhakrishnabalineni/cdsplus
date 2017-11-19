package com.hp.soar.priorityLoader.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.vfs.FileSystemException;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.hp.loader.utils.ConfigurationReader;

public class VirusScanner {
	// Config labels
	private static final String VIRUSSCANELEM = "virusScan";
	private static final String SCANFLAG = "scan";
	private static final String INSTALLDIR = "installDir";
	private static final String SCANCMD = "scanCommand";
	private static final String VERSIONCMD = "versionCommand";
	private static final String SCANCONFIGFILES = "configFiles"; 

	// virus scan settings 
	private static Calendar lastLogTime;
	private static boolean scanOn;
	private static File[] configFiles;
	private static long[] configTimeStamps;
	private static File	installDir;
	private static String versionCmd;
	private static String scanCmd;
	// flag to indicate an exit has been requested
	private static boolean exit = false;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_sHH:mm:ss");

	// Logger for virus scan output
	private static Logger logger = Logger.getLogger(VirusScanner.class.getName());


	private String scanCmdInst = null;
	private File scanDir;
	private String collectionId;

	public VirusScanner(File scanDir, String collectionId) {
		super();
		if (!scanOn) {
			return;
		}
		this.collectionId = collectionId;
		scanCmdInst = scanCmd + scanDir.getAbsolutePath();
		this.scanDir = scanDir;
	}

	/**
	 * virusScan walks an entire Collection directory and checks every file in it.  This reduces the 30 second overhead of loading the scanner
	 * for every file.
	 * @param dir
	 * @throws VirusScanException
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void scan() throws VirusScanException, IOException, InterruptedException {
		if (LoaderLog.isInfoEnabled()) {
			LoaderLog.info("Start scan: "+collectionId);
		}
		if (!scanOn) {
			// not scanning so just return
			if (LoaderLog.isDebugEnabled()) {
				LoaderLog.debug("Scan off.");
			}
			return;
		}

		logConfig();

		// see if the workDir exists.  If it doesn't, there wasn't anything checked out for
		// deployment
		if (!scanDir.exists()) {
			if (LoaderLog.isInfoEnabled()) {
				LoaderLog.info("Nothing is being deployed. Scan skipped.");
			}
			return;
		}

		long start = System.currentTimeMillis();

		//Execute a shell for the targeting script
		Process shell = Runtime.getRuntime().exec(scanCmdInst);

		if (shell == null){
			LoaderLog.error("Virus scanner failed to start");
			throw new VirusScanException("Scan failed to run!");
		}

		// Spawn a thread to read stderr
		StreamConsumer errorStream = new StreamConsumer(shell.getErrorStream(), "ScanErr");

		// Spawn a thread to read stdout
		StreamConsumer outputStream = new StreamConsumer(shell.getInputStream(), "ScanOut");

		// Wait until the scanner finish and return exit value
		int shellExitStatus = -1;
		boolean done = false;
		if (exit) {
			// don't start the scan, just throw the exception to indicate this was interrupted
			throw new InterruptedException("Exit requested before scanning.");
		}

		while (!done) {
			try {
				shellExitStatus = shell.waitFor();
				done = true;
			} catch (InterruptedException e) {
				if (exit) {
					// got interrupted because exit has been requested so throw interrupted exception
					throw new InterruptedException("Scan interrupted");
				}
			}
		}

		String msg = null;
		if (shellExitStatus != 0) {
			// Handle different scan return values
			switch (shellExitStatus) {
			case 1:
				msg = "Scanner is not running.";
				break;
			case 2:
				msg = "Failed integrity check on DAT file.";
				break;
			case 6:
				msg = "No DAT file.";
				break;
			case 8:
				msg = "No DAT file.";
				break;
			case 13:
				msg = "Virus Found!!!";
				break;
			default:
				msg = "Unknown virus scan return code: "+shellExitStatus;
				break;
			}
			LoaderLog.error(scanDir.getAbsolutePath()+" : "+msg);
			errorStream.close();
			outputStream.close();
			// print the error message to the log
			writeToLog (outputStream);
			throw new VirusScanException ("Scan Failed: "+msg);
		}

		LoaderLog.info("Scan Completed: "+collectionId+" <"+(System.currentTimeMillis() - start)+">");

		// close the streams
		errorStream.close();
		outputStream.close();
		
		// write the output to the log file.
		writeToLog (outputStream);
		
	}
	
	// log the files that were scanned
	private void writeToLog (StreamConsumer stream)	{
		String[] output = stream.getResult().split("\n");

		int idx = -1;
		String key = collectionId.toLowerCase();
		for(String line : output) {
			if ((idx = line.indexOf(key)) != -1) {
				logger.info(line.substring(idx));
			}
		}
	}
	/**
	 * logConfig puts the version information into the virus scan log if it is either the first run after midnight or
	 * one of the config files has changed.
	 * @throws IOException 
	 * @throws VirusScanException 
	 */
	private static void logConfig() throws IOException, VirusScanException {
		synchronized(configFiles) {
			boolean configChanged = false;
			for(int i=0; i<configFiles.length; i++) {
				long newTS = configFiles[i].lastModified();
				if (newTS != configTimeStamps[i]) {
					configChanged = true;
					configTimeStamps[i] = newTS;
				}
			}

			Calendar now = Calendar.getInstance();
			if (configChanged || (lastLogTime == null) || (now.get(Calendar.WEEK_OF_YEAR) != lastLogTime.get(Calendar.WEEK_OF_YEAR))){
				// need to write out the log header
				LoaderLog.info("Update virus config specs");
				//Execute a shell for the targeting script
				Process shell = Runtime.getRuntime().exec(versionCmd,null,installDir);

				if (shell == null){
					throw new VirusScanException("Scan failed to update virus config in log file!");
				}

				// Spawn a thread to read stdout
				StreamConsumer outputStream = new StreamConsumer(shell.getInputStream(), "SCAN_VERSION_OUT");

				// Spawn a thread to read stderr
				StreamConsumer errorStream = new StreamConsumer(shell.getErrorStream(), "SCAN_VERSION_ERR");

				// Wait until the scanner finish and return exit value
				try {
					int shellExitStatus = shell.waitFor();
					if (shellExitStatus != 0) {
						// didn't get the virus definitions from version command
						throw new VirusScanException("Scan call to get version failed. Status: "+shellExitStatus);
					}
				} catch (InterruptedException e) {
					throw new VirusScanException("Scan intrupted during virus config update to log file!");
				}

				errorStream.close();
				outputStream.close();

				// write out the logger info
				// log the files that were scanned
				String[] output = outputStream.getResult().split("\n");
				for(String line : output) {
					logger.info(line);
				}

				// reset lastLogTime
				lastLogTime = now;
			}
		}
	}

	/**
	 * getAttribute checks that all attributes requested are set
	 * @param elem
	 * @param field
	 * @return
	 */
	private static String getAttribute(Element elem, String field) {
		String result = elem.getAttribute(field);
		if ((result == null) || result.length()==0) {
			throw new IllegalArgumentException(field+" not specified in config file.");
		}
		return result;
	}

	/**
	 * Receive notification that an exit has been requested.
	 */
	public static void exit() {
		exit = true;
	}

	/**
	 * init configures the SFTPDestination and gets the serverOid for soar
	 * @param config
	 * @throws FileSystemException 
	 */
	public static void init(ConfigurationReader config) {
		Element vsElement = config.getElement(VIRUSSCANELEM);
		if (vsElement == null) {
			throw new IllegalArgumentException(VIRUSSCANELEM+" not specified in config file.");
		}

		String scanFlag = getAttribute(vsElement, SCANFLAG);
		scanOn = "on".equalsIgnoreCase(scanFlag);
		String instDir = getAttribute(vsElement, INSTALLDIR); 
		installDir = new File(instDir);
		String configDatFilesStr = getAttribute(vsElement, SCANCONFIGFILES);
		scanCmd = getAttribute(vsElement, SCANCMD);
		versionCmd = getAttribute(vsElement, VERSIONCMD);
		String[] configDatFiles = configDatFilesStr.split("\\|");
		configFiles = new File[configDatFiles.length];
		configTimeStamps = new long[configDatFiles.length];
		for(int i=0; i<configFiles.length; i++) {
			configFiles[i] = new File(installDir+configDatFiles[i]);
			configTimeStamps[i] = configFiles[i].lastModified();
		}
		lastLogTime = null;

		try {
			if (scanOn) {
				VirusScanner.logConfig();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new IllegalArgumentException("Virus Scanner Failed to get setup logged.",e);
		} catch (VirusScanException e) {
			throw new IllegalArgumentException("Virus Scanner Failed to get setup logged.",e);
		}
	}
}
