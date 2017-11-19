package com.hp.cdsplus.pmloader;

import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The class <code>TestAll</code> builds a suite that can be used to run all
 * of the tests within its package as well as within any subpackages of its
 * package.
 *
 * @generatedBy CodePro at 6/18/13 5:29 PM
 * @author kashyaks
 * @version $Revision: 1.0 $
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	PMasterLoaderTest.class,
	LevelTest.class,
	com.hp.cdsplus.pmloader.extractor.TestAll.class,
	com.hp.cdsplus.pmloader.item.TestAll.class,
	com.hp.cdsplus.pmloader.queue.TestAll.class,
	com.hp.cdsplus.pmloader.worker.TestAll.class,
})
public class TestAll {

	/**
	 * Launch the test.
	 *
	 * @param args the command line arguments
	 *
	 * @generatedBy CodePro at 6/18/13 5:29 PM
	 */
	public static void main(String[] args) {
		JUnitCore.runClasses(new Class[] { TestAll.class });
	}
}
