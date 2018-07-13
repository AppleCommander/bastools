package io.github.applecommander.bastools.api.directives;

import java.io.IOException;
import java.io.OutputStream;

import io.github.applecommander.bastools.api.Configuration;
import io.github.applecommander.bastools.api.Directive;
import io.github.applecommander.bastools.api.model.ApplesoftKeyword;
import io.github.applecommander.bastools.api.model.Line;

/** 
 * A simple directive to introduce hexadecimal capabilities. StreamTokenizer does not 
 * appear to support syntax, so using a directive to introduce the capability. 
 */
public class HexDirective extends Directive {
    public static final String NAME = "$hex";
    
	public HexDirective(Configuration config, OutputStream outputStream) {
		super(NAME, config, outputStream);
	}
	
	@Override
	public void writeBytes(int startAddress, Line line) throws IOException {
	    int value = requiredIntegerExpression("value", "$hex directive requires 'value' parameter");
		
		if (value < 0 || value > 65535) {
			throw new RuntimeException("$hex address out of range");
		}
		
		String value1 = Integer.toString(value);
		String value2 = Integer.toString(value - 65536);

		String shortestValue = value1.length() < value2.length() ? value1 : value2;
		
		if (shortestValue.startsWith("-")) {
			outputStream.write(ApplesoftKeyword.sub.code);
			shortestValue = shortestValue.substring(1);
		}
		outputStream.write(shortestValue.getBytes());
	}
}
