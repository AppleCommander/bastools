package io.github.applecommander.bastokenizer.api.directives;

import java.io.IOException;
import java.io.OutputStream;

import io.github.applecommander.bastokenizer.api.Configuration;
import io.github.applecommander.bastokenizer.api.Directive;
import io.github.applecommander.bastokenizer.api.model.ApplesoftKeyword;
import io.github.applecommander.bastokenizer.api.model.Line;

/** 
 * A simple directive to introduce hexidecimal capabilities. StreamTokenizer does not 
 * appear to support syntax, so using a directive to introduce the capability. 
 */
public class HexDirective extends Directive {
	public HexDirective(Configuration config, OutputStream outputStream) {
		super(config, outputStream);
	}

	@Override
	public void writeBytes(int startAddress, Line line) throws IOException {
		if (parameters.size() != 1) {
			throw new RuntimeException("$hex directive requires one parameter");
		}
		String string = requiresString();
		int value = Integer.parseInt(string, 16);
		
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
