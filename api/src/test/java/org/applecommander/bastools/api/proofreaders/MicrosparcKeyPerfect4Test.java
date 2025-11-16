package org.applecommander.bastools.api.proofreaders;

import org.applecommander.bastools.api.proofreaders.MicrosparcKeyPerfect4.Checksum;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MicrosparcKeyPerfect4Test {
    @Test
    public void testChecksum() {
        // This is "filtered" already so we are just testing the checksum
        final int[] code = {
            0x0A, 0x00, 0x89, 0x3A, 0x97,                                   // 10 TEXT HOME
            0x14, 0x00, 0xBA, 0x22, 0x48, 0x45, 0x4C, 0x4C, 0x4F, 0x22,     // 20 PRINT "HELLO"
            0x1E, 0x00, 0x80                                                // 30 END
        };
        Checksum checksum = new Checksum();
        for (int b : code) {
            checksum.addByte(b);
        }
        assertEquals(0x783, checksum.getChecksum());
    }
}
