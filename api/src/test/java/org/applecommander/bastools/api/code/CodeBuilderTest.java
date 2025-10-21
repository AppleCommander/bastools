/*
 * bastools
 * Copyright (C) 2025  Robert Greene
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
package org.applecommander.bastools.api.code;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import org.junit.Test;

import org.applecommander.bastools.api.model.ApplesoftKeyword;

public class CodeBuilderTest {
    @Test
    public void testBasicRETURN() throws IOException {
        final byte[] expected = { (byte)ApplesoftKeyword.RETURN.code, 0x00 };
        
        CodeBuilder builder = new CodeBuilder();
        builder.basic().RETURN().endLine();
        
        assertArrayEquals(expected, builder.generate(0x0000).toByteArray());
    }
    
    @Test
    public void testAsmWithMark() throws IOException {
        final byte[] data = { 0x01, 0x02, 0x03 };
        final byte[] expected = { 
                (byte)0xa9, 0x09,       // 0x801: LDA #$09 
                (byte)0x85, (byte)0xad, // 0x803: STA $AD
                (byte)0xa9, 0x08,       // 0x805: LDA #$08
                (byte)0x85, (byte)0xae, // 0x807: STA $AE
                0x01, 0x02, 0x03        // 0x809: 01 02 03 ("data")
            };
        
        CodeBuilder builder = new CodeBuilder();
        CodeMark mark = new CodeMark();
        builder.asm()
               .setAddress(mark, 0xad)
               .end()
               .set(mark)
               .addBinary(data);
        
        assertArrayEquals(expected, builder.generate(0x801).toByteArray());
    }
}
