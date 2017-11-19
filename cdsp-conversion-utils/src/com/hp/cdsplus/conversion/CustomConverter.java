/**
 * 
 */
package com.hp.cdsplus.conversion;

import org.codehaus.jettison.mapped.TypeConverter;

/**
 * @author srivasni
 *
 */
public class CustomConverter implements TypeConverter {

	private static final String ENFORCE_32BIT_INTEGER_KEY = "jettison.mapped.typeconverter.enforce_32bit_integer";
	public static final boolean ENFORCE_32BIT_INTEGER = Boolean
			.getBoolean(ENFORCE_32BIT_INTEGER_KEY);
	private boolean enforce32BitInt;

	public CustomConverter() {
		this.enforce32BitInt = ENFORCE_32BIT_INTEGER;
	}

	public void setEnforce32BitInt(boolean enforce32BitInt) {
		this.enforce32BitInt = enforce32BitInt;
	}

	public Object convertToJSONPrimitive(String text) {
		if (text == null)
			return text;
		Object primitive = null;
		try {
			primitive = Long.valueOf((this.enforce32BitInt) ? Integer.valueOf(
					text).intValue() : Long.valueOf(text).longValue());
		} catch (Exception e) {
		}
		if (primitive == null)
			try {
				Double v = Double.valueOf(text);
				if ((!(v.isInfinite())) && (!(v.isNaN()))) {
					primitive = v.toString();
				} else
					primitive = text;
			} catch (Exception e) {
			}
		if ((primitive == null)
				&& (((text.trim().equalsIgnoreCase("true")) || (text.trim()
						.equalsIgnoreCase("false"))))) {
			primitive = Boolean.valueOf(text);
		}

		if ((primitive == null) || (!(primitive.toString().equals(text)))) {
			primitive = text;
		}

		return primitive;
	}

}
