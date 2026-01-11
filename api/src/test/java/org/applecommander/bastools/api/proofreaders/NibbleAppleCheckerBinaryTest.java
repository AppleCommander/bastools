package org.applecommander.bastools.api.proofreaders;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NibbleAppleCheckerBinaryTest {
    @Test
    public void testBinary() {
        runTest(0x300, 0x7a,
                0xa9, 0x01, 0x8d, 0x00, 0x0a, 0xad, 0x00, 0x0a,     // 0300-
                0x20, 0xa8, 0xfc, 0x20, 0xe4, 0xfb, 0x4c, 0x05,            // 0308-
                0x03);                                                     // 0310-
    }

    public void runTest(int address, int expectedChecksum, int... bytes) {
        byte[] code = new byte[bytes.length];
        for (int i=0; i<bytes.length; i++) {
            code[i] = (byte) bytes[i];
        }

        NibbleAppleCheckerBinary proofreader = new NibbleAppleCheckerBinary("test.bin");
        proofreader.addBytes(address, code);
        assertEquals(bytes.length, proofreader.getLength());
        assertEquals(expectedChecksum, proofreader.getChecksumValue());
    }
}
