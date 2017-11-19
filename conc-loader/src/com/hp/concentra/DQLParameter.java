package com.hp.concentra;

/**
 * This class is used to create an object that represent a DQL parameter to be
 * used to replace the ? character (parameter placeholder) in DQL statements.
 * 
 * @author GADSC IPG-IT CR
 * @version %I%, %G%
 * @since 1.0
 */
public class DQLParameter {

    // Constants

    // enum for parameter type
    public static final short DQL_STRING = 0;
    public static final short DQL_LABEL = 1;
    public static final short DQL_INTEGER = 2;

    // Attributes

    /**
     * Attribute that represents the parameter value
     */
    private Object value;

    /**
     * Attribute that represents the parameter type
     */
    private short type;

    // Constructors

    /**
     * Constructs a newly allocated DQLParameter object, assigning by default
     * DQL_STRING as the parameter type
     * 
     * @param value
     *            the parameter value
     */
    public DQLParameter(Object value) {

        this.type = DQL_STRING;
        this.value = value;
    }

    /**
     * Constructs a newly allocated DQLParameter object
     * 
     * @param value
     *            the parameter value
     * @param type
     *            the parameter type
     */
    public DQLParameter(Object value, short type) {

        this.type = type;
        this.value = value;
    }

    /**
     * Generates the String value of the parameter according to its type for DQL
     * statement string generation
     * 
     * @return a String value of the parameter value according to its type. For
     *         example, if the parameter type is DQL_STRING, it returns its
     *         value surrounded by the QUOTE(') character
     */
    public String toDQLQueryString() {
        String result = null;

        // Define the format of the string value to return
        switch (type) {
            case DQL_STRING:
            	// quote the string if it isn't already quoted
            	String val = value.toString();
            	if (val.startsWith("\'")) {
            		result = val;
            	} else {
                result = "'".concat(val).concat("'");
            	}
            	break;
            case DQL_LABEL:
            case DQL_INTEGER:
            default:
                result = value.toString();
                break;
        }
        return result;
    }

}
