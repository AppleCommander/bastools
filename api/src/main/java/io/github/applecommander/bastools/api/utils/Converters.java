package io.github.applecommander.bastools.api.utils;

import java.util.Arrays;
import java.util.stream.IntStream;

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

	/**
	 * Convert a string to a boolean value allowing for "true" or "yes" to evaluate to Boolean.TRUE.
	 */
	public static Boolean toBoolean(String value) {
	    if (value == null) {
	        return null;
	    }
	    return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value);
	}
	
    
    /** 
     * Supports entry of values in ranges or comma-separated lists and combinations thereof.
     * <ul>
     * <li>Range: <code>m-n</code> where m<n.</li>
     * <li>Distinct values: <code>a,b,c,d</code>.</li>
     * <li>Single value: <code>x</code></li>
     * <li>Combination: <code>m-n;a,b,c,d;x</code>.</li>
     * </ul>
     */
    public static IntStream toIntStream(String values) {
        IntStream stream = IntStream.empty();
        for (String range : values.split(";")) {
            if (range.contains("-")) {
                String[] parts = range.split("-");
                int low = Integer.parseInt(parts[0]);
                int high = Integer.parseInt(parts[1]);
                stream = IntStream.concat(stream, IntStream.rangeClosed(low, high));
            } else {
                stream = IntStream.concat(stream, 
                        Arrays.stream(range.split(",")).mapToInt(Integer::parseInt));
            }
        }
        return stream;
    }
}
