package org.applecommander.bastools.api.proofreaders;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Perform some rudimentary testing of the Compute! Apple Automatic Proofreader algorithm.
 * Note that some of the algorithm is replicated in the "perform" method.
 */
public class ComputeAutomaticProofreaderTest {
    @Test
    public void testLineChecksums() {
        assertEquals(0x4a, performLineCalc("10 HOME"));
        assertEquals(0x52, performLineCalc("20 D$ = CHR$(4)"));
        assertEquals(0x25, performLineCalc("40 PRINT \"DO YOU WANT TO:\""));
    }

    protected int performLineCalc(String text) {
        int checksum = 0;
        for (int i=text.length()-1; i>=0; i--) {
            char ch = text.charAt(i);
            if (ch != ' ') {
                checksum = ComputeAutomaticProofreader.proofreader(checksum, ch|0x80);
            }
        }
        return checksum;
    }
}
