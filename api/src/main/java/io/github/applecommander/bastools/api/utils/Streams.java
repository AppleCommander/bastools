package io.github.applecommander.bastools.api.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Streams {
	private Streams() { /* Prevent construction */ }
	
	/** Utility method to read all bytes from an InputStream. */
	public static byte[] toByteArray(InputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		while (true) {
			byte[] buf = new byte[1024];
			int len = inputStream.read(buf);
			if (len == -1) break;
			outputStream.write(buf, 0, len);
		}
		outputStream.flush();
		return outputStream.toByteArray();
	}

}
