package org.applecommander.bastools.api.proofreaders;

public class NibbleCheckitBinary implements BinaryInputBufferProofReader {
    private final NibbleCheckitChecksum lineChecksum = new NibbleCheckitChecksum();
    private final NibbleCheckitChecksum totalChecksum = new NibbleCheckitChecksum();

    @Override
    public void addProgram(final String code) {
        System.out.println("Nibble Checkit, Copyright 1988, Microsparc Inc.");
        BinaryInputBufferProofReader.super.addProgram(code);
        System.out.printf("TOTAL: %04X\n", getTotalChecksumValue());
    }

    @Override
    public void addLine(final String line) {
        lineChecksum.reset();
        for (char ch : line.toCharArray()) {
            lineChecksum.add(ch|0x80);
        }
        System.out.printf("%02X | %s\n", getLineChecksumValue(), line);

        var info = BinaryDataProofReader.parseLine(line);
        for (byte b : info.code()) {
            totalChecksum.add(Byte.toUnsignedInt(b));
        }
    }

    public int getLineChecksumValue() {
        return ( (lineChecksum.value() & 0xff) - (lineChecksum.value() >> 8) ) & 0xff;
    }
    public int getTotalChecksumValue() {
        int high = totalChecksum.value() & 0xff;
        int low = (totalChecksum.value() >> 8) & 0xff;
        return high << 8 | low;
    }
}
