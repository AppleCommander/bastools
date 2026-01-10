package org.applecommander.bastools.api.proofreaders;

import org.applecommander.bastools.api.Configuration;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class NibbleAppleCheckerTest {
    @Test
    public void testChecksum() {
        // Based on 6502 assembly run - checks step-by-step values
        NibbleAppleChecker.Checksum checksum = new NibbleAppleChecker.Checksum();
        checksum.add(0x89);     // TEXT
        assertEquals(0x13, checksum.value());
        checksum.add(0x3a);     // :
        assertEquals(0x65, checksum.value());
        checksum.add(0x97);     // HOME
        assertEquals(0x4b, checksum.value());
    }

    @Test
    public void testCode() {
        Configuration config = Configuration.builder()
                .startAddress(0xb01)
                .sourceFile(new File("test.bas"))
                .build();
        NibbleAppleChecker checker = new NibbleAppleChecker(config);
        // 10 TEXT:HOME as a sample...
        byte[] code = { 0x09, 0x0b, 0x0a, (byte)0x89, 0x3a, (byte)0x97, 0x00, 0x00, 0x00, 0x00, 0x00 };
        checker.addBytes(code);
        assertEquals(0x2, checker.getLength());
        assertEquals(0x4b, checker.getChecksumValue());
    }
}
