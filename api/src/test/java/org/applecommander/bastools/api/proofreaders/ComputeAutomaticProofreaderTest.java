/*
 * bastools
 * Copyright (C) 2026  Robert Greene
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
