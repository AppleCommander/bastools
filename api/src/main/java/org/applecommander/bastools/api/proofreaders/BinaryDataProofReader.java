package org.applecommander.bastools.api.proofreaders;

import java.io.ByteArrayOutputStream;

/**
 * Standard interface for proofreaders that evaluate a binary program.
 */
public interface BinaryDataProofReader {
    /** Parses each line into a byte stream and then uses addBytes to roll into checksum. */
    default void addProgram(String text) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int address = 0;
        for (var line : text.lines().toList()) {
            var info = parseLine(line);
            if (address == 0) address = info.address;
            outputStream.writeBytes(info.code);
        };
        addBytes(address, outputStream.toByteArray());
    }
    /** Parses the line and adds to the current checksums. */
    void addBytes(int address, byte... binary);
    /** Handy method to parse a line from text into address + bytes. */
    static LineInfo parseLine(final String line) {
        String[] parts = line.split(":");
        assert parts.length != 0;
        int address = Integer.parseInt(parts[0], 16);
        String[] bytes = parts[1].split(" ");
        assert bytes.length > 0;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (String value : bytes) {
            outputStream.write(Integer.parseInt(value, 16));
        }
        return new LineInfo(address, outputStream.toByteArray(), line);
    }
    record LineInfo(int address, byte[] code, String line) {}
}
