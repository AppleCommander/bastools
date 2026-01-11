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
package org.applecommander.bastools.api;

import org.applecommander.bastools.api.model.Token;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Configuration {
	public final File sourceFile;
	public final int startAddress;
	public final int maxLineLength;
	public final PrintStream debugStream;
	public final Map<String,String> variableReplacements = new HashMap<>();
    public final boolean preserveNumbers;
	
	private Configuration(Builder b) {
		this.sourceFile = b.sourceFile;
		this.startAddress = b.startAddress;
		this.maxLineLength = b.maxLineLength;
		this.debugStream = b.debugStream;
        this.preserveNumbers = b.preserveNumbers;
	}

    public String numberToString(Token token) {
        if (preserveNumbers && token.text() != null) {
            return token.text();
        }
        else {
            if (Math.rint(token.number()) == token.number()) {
                return Integer.toString(token.number().intValue());
            } else {
                return Double.toString(token.number());
            }
        }
    }
	
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder {
		private Builder() { /* Prevent construction */ }
		
		private File sourceFile;
		private int startAddress = 0x801;
		private int maxLineLength = 255;
		private PrintStream debugStream = new PrintStream(new OutputStream() {
				@Override
				public void write(int b) {
					// Do nothing
				}
			});
        private boolean preserveNumbers;

		public Builder sourceFile(File sourceFile) {
			this.sourceFile = sourceFile;
			return this;
		}
		public Builder startAddress(int startAddress) {
			this.startAddress = startAddress;
			return this;
		}
		public Builder maxLineLength(int maxLineLength) {
			this.maxLineLength = maxLineLength;
			return this;
		}
		public Builder debugStream(PrintStream debugStream) {
			this.debugStream = debugStream;
			return this;
		}
        public Builder preserveNumbers(boolean preserveNumbers) {
            this.preserveNumbers = preserveNumbers;
            return this;
        }
		
		public Configuration build() {
			Objects.requireNonNull(sourceFile, "Please configure a sourceFile");
			Objects.requireNonNull(debugStream, "debugStream cannot be null");
			return new Configuration(this);
		}
	}
}
