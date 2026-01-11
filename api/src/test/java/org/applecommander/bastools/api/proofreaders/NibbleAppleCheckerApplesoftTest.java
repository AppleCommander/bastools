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

public class NibbleAppleCheckerApplesoftTest {
    @Test
    public void testChecksum() {
        // Based on 6502 assembly run - checks step-by-step values
        NibbleAppleCheckerChecksum checksum = new NibbleAppleCheckerChecksum();
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
        NibbleAppleCheckerApplesoft checker = new NibbleAppleCheckerApplesoft(config);
        // 10 TEXT:HOME as a sample...
        byte[] code = { 0x09, 0x0b, 0x0a, (byte)0x89, 0x3a, (byte)0x97, 0x00, 0x00, 0x00, 0x00, 0x00 };
        checker.addBytes(code);
        assertEquals(0x2, checker.getLength());
        assertEquals(0x4b, checker.getChecksumValue());
    }
}
