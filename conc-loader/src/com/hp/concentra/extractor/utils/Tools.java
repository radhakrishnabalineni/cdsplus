package com.hp.concentra.extractor.utils;

/**
 * Utility class. Every method or variable used in a general way has to be
 * included in this class.
 * 
 * 
 * @author GADSC IPG-IT CR
 * @version 1.0
 * @since 1.0
 */

public class Tools {

    /**
     * Check if the value set in a String Object representing a token contains a
     * valid integer value.
     * 
     * @param value
     *            String containing the token value to be evaluated
     * @return true if the token is valid, false if not
     */
    public static boolean checkTokenFormat(String token) {
        boolean tokenGood = true;
        token = token.trim();
        try {
            Integer.parseInt(token);
        } catch (NumberFormatException e) {
            tokenGood = false;
        }
        return tokenGood;
    }

    /**
     * Output informing the state of the memory related to the Java virtual
     * machine. Print out the total memory assigned, the total memory used and
     * finally the total memory free.
     * 
     * @return String value with the information of the memory
     */
    public static String memorySnapshot() {
        try {
            runGC();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "[Tools:memorySnapShot]: Limit=".concat(bytesToKBStr(Runtime.getRuntime().maxMemory())).concat(
                ", Total=").concat(bytesToKBStr(Runtime.getRuntime().totalMemory())).concat(", Used=").concat(
                bytesToKBStr(memoryInUse())).concat(", Free=").concat(bytesToKBStr(Runtime.getRuntime().freeMemory()));
    }

    /**
     * Calculates the amount of memory in use related to the Java virtual
     * machine
     * 
     * @return long number representing the bytes in use
     */
    private static long memoryInUse() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    /**
     * Transforms a long input representing bytes, to the Kilobytes and resting
     * bytes in a string that can be used in human read output.
     * 
     * @param bytes
     *            a long value representing the amount to transform
     * @return a String value with the amount transformed
     */
    private static String bytesToKBStr(long bytes) {
        long kb = bytes / 1024;
        long b = bytes % 1024;

        return kb + "." + b + "KB (" + bytes + " bytes)";
    }

    /**
     * Call gc() runtime method is just a message to the garbage collector
     * saying that run in the moment is a great idea, but, most of the time
     * doesn't performance the action indeed. After a set of gc() calls the
     * collector really runs. The strategy is made multiple calls to gc() method
     * to trigger the process.
     * 
     * @throws Exception
     */
    private static void runGC() throws Exception {

        long usedMem1 = memoryInUse();
        long usedMem2 = Long.MAX_VALUE;

        for (int i = 0; (usedMem1 < usedMem2) && (i < 500); ++i) {
            Runtime.getRuntime().runFinalization();
            Runtime.getRuntime().gc();
            Thread.yield();

            usedMem2 = usedMem1;
            usedMem1 = memoryInUse();
            ;
        }
    }

}
