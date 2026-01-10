package org.applecommander.bastools.api.proofreaders;

import org.applecommander.bastools.api.Configuration;
import org.junit.Test;

import java.io.File;

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
        Configuration config = Configuration.builder()
                .preserveNumbers(true)
                .sourceFile(new File("test.bas"))
                .build();
        ComputeAutomaticProofreader proofreader = new ComputeAutomaticProofreader(config);
        proofreader.addLine(text);
        return proofreader.getChecksumValue();
    }
}
