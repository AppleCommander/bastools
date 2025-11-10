package org.applecommander.bastools.api.proofreaders;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Perform some rudimentary testing of the Nibble Checkit algorithm.
 * Note that some of the algorithm is replicated in the "perform" methods.
 */
public class NibbleCheckitTest {
    @Test
    public void testLineChecksums() {
        assertEquals(0x37, performLineCalc("10 REM"));
        assertEquals(0x54, performLineCalc("20 FOR J=1 TO 5:PRINT CHR$(7):NEXT J"));
        assertEquals(0x91, performLineCalc("30 END"));
    }

    @Test
    public void testProgramChecksum() {
        // Note: The value displayed is 1CB9, but the code prints low byte first...
        assertEquals(0xb91c, performProgramCalc(10, 20, 30));
    }

    protected int performLineCalc(String text) {
        int checksum = 0;
        for (char ch : text.toCharArray()) {
            if (ch != ' ') {
                checksum = NibbleCheckit.checkit(checksum, ch | 0x80);
            }
        }
        return ( (checksum & 0xff) - (checksum >> 8) ) & 0xff;
    }

    protected int performProgramCalc(int... lineNumbers) {
        int checksum = 0;
        for (int lineNumber : lineNumbers) {
            checksum = NibbleCheckit.checkit(checksum, lineNumber & 0xff);
            checksum = NibbleCheckit.checkit(checksum, lineNumber >> 8);
        }
        return checksum;
    }
}
