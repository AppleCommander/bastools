package org.applecommander.bastools.api.proofreaders;

public class NibbleAppleCheckerBinary implements BinaryDataProofReader {
    private final String filename;
    private int length;
    private final NibbleAppleCheckerChecksum checksum = new NibbleAppleCheckerChecksum();

    public NibbleAppleCheckerBinary(String filename) {
        this.filename = filename;
    }

    @Override
    public void addBytes(int address, byte... binary) {
        for (byte b : binary) {
            checksum.add(Byte.toUnsignedInt(b));
            length++;
        }

        System.out.printf("On: %s\n", filename);
        System.out.println("Type: B");
        System.out.println();
        System.out.printf("Length: %04X\n", length);
        System.out.printf("Checksum: %02X\n", checksum.value());
    }

    public int getLength() {
        return length;
    }
    public int getChecksumValue() {
        return checksum.value();
    }
}
