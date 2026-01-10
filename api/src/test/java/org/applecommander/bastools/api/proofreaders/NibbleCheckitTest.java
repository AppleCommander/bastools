package org.applecommander.bastools.api.proofreaders;

import org.applecommander.bastools.api.Configuration;
import org.junit.Test;

import java.io.File;

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
    public void testQuestionMark49() {
        assertEquals(0xf6, performLineCalc("110  PRINT \"HELLO WORLD?\""));
        assertEquals(0xe7, performLineCalc("10 PRINT \"?\""));
    }

    @Test
    public void testProgramChecksum() {
        // Note: The value displayed is 1CB9, but the code prints low byte first...
        assertEquals(0xb91c, performProgramCalc(10, 20, 30));
    }

    protected int performLineCalc(String text) {
        Configuration config = Configuration.builder()
                .preserveNumbers(true)
                .sourceFile(new File("test.bas"))
                .build();
        NibbleCheckit proofreader = new NibbleCheckit(config);
        proofreader.addLine(text);
        return ( (proofreader.getLineChecksumValue() & 0xff) - (proofreader.getLineChecksumValue() >> 8) ) & 0xff;
    }

    protected int performProgramCalc(int... lineNumbers) {
        Configuration config = Configuration.builder()
                .preserveNumbers(true)
                .sourceFile(new File("test.bas"))
                .build();
        NibbleCheckit proofreader = new NibbleCheckit(config);
        for (int lineNumber : lineNumbers) {
            // We ignore the line, but line also computes the program checksum
            proofreader.addLine(Integer.toString(lineNumber));
        }
        return proofreader.getTotalChecksumValue();
    }
}
