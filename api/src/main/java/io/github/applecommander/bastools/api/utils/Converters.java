package io.github.applecommander.bastools.api.utils;

public class Converters {
	private Converters() { /* Prevent construction */ }
	
	/** 
	 * Convert a string to an integer allowing multiple formats.  
	 * Normal decimal, or hexadecimal with a <code>$</code> or <code>0x</code> prefix. 
	 */
	public static Integer toInteger(String value) {
		if (value == null) {
			return null;
		} else if (value.startsWith("$")) {
			return Integer.valueOf(value.substring(1), 16);
		} else if (value.startsWith("0x") || value.startsWith("0X")) {
			return Integer.valueOf(value.substring(2), 16);
		} else {
			return Integer.valueOf(value);
		}
	}

}
