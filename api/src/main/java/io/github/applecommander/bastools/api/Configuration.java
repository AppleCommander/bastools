package io.github.applecommander.bastools.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;

public class Configuration {
	public final File sourceFile;
	public final int startAddress;
	public final int maxLineLength;
	public final PrintStream debugStream;
	
	private Configuration(Builder b) {
		this.sourceFile = b.sourceFile;
		this.startAddress = b.startAddress;
		this.maxLineLength = b.maxLineLength;
		this.debugStream = b.debugStream;
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
				public void write(int b) throws IOException {
					// Do nothing
				}
			});

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
		
		public Configuration build() {
			Objects.requireNonNull(sourceFile, "Please configure a sourceFile");
			Objects.requireNonNull(debugStream, "debugStream cannot be null");
			return new Configuration(this);
		}
	}
}
