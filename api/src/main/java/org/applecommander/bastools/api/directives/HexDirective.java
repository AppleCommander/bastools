/*
 * bastools
 * Copyright (C) 2025  Robert Greene
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.applecommander.bastools.api.directives;

import java.io.IOException;
import java.io.OutputStream;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.Directive;
import org.applecommander.bastools.api.model.ApplesoftKeyword;
import org.applecommander.bastools.api.model.Line;

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
