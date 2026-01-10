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
            checksum.add(b);
        }
        assertEquals(0x783, checksum.value());
    }
}
