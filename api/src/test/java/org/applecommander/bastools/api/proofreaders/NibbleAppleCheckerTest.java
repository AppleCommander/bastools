package org.applecommander.bastools.api.proofreaders;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NibbleAppleCheckerTest {
    @Test
    public void testChecksum() {
        // Based on 6502 assembly run
        assertEquals(0x13, NibbleAppleChecker.checksum(0x0, 0x89));     // TEXT
        assertEquals(0x65, NibbleAppleChecker.checksum(0x13, 0x3a));    // :
        assertEquals(0x4b, NibbleAppleChecker.checksum(0x65, 0x97));    // HOME
    }

    @Test
    public void testCode() {
        NibbleAppleChecker.Checksum cksum = new NibbleAppleChecker.Checksum();
        // 10 TEXT:HOME as a sample...
        byte[] code = { 0x09, 0x0b, 0x0a, (byte)0x89, 0x3a, (byte)0x97, 0x00, 0x00, 0x00, 0x00, 0x00 };
        cksum.compute(code, 0xb01);
        //assertEquals(0x2, cksum.getLength());
        assertEquals(0x4b, cksum.getChecksum());
    }
}
