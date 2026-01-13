package org.applecommander.bastools.api.proofreaders;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * Standard interface for proofreaders that evaluate a binary program.
 */
public interface BinaryInputBufferProofReader {
    default void addProgram(String code) {
        code.lines().forEach(this::addLine);
    }
    /** Parses the line and adds to the current checksums. */
    void addLine(String line);
    /** Converts to standard lines adds to the current checksums. */
    default void addBytes(int address, byte... binary) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(outputStream);
        for (int i=0; i<binary.length; i++) {
            if (i % 16 == 0) {
                if (i > 0) pw.println();
                pw.printf("%04X:", address+i);
            }
            else {
                pw.print(' ');
            }
            pw.printf("%02X", binary[i]);
        }
        addProgram(outputStream.toString());
    }
}
