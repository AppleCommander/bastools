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
package org.applecommander.bastools.api.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConverterTest {
    @Test
    public void testToInteger() {
        assertEquals(0x1000, Converters.toInteger("0x1000").intValue());
        assertEquals(0x1000, Converters.toInteger("0X1000").intValue());
        assertEquals(0x1000, Converters.toInteger("$1000").intValue());
        assertEquals(1000, Converters.toInteger("1000").intValue());
        assertNull(Converters.toInteger(null));
    }
    
    @Test
    public void testToBoolean() {
        assertTrue(Converters.toBoolean("true"));
        assertTrue(Converters.toBoolean("True"));
        assertTrue(Converters.toBoolean("YES"));
        assertFalse(Converters.toBoolean("faLse"));
        assertFalse(Converters.toBoolean("No"));
        assertFalse(Converters.toBoolean("notreally"));
        assertNull(Converters.toBoolean(null));
    }
    
    @Test
    public void testToIntStream_Range() {
        final int[] expected = { 4, 5, 6, 7, 8 };
        assertArrayEquals(expected, Converters.toIntStream("4-8").toArray());
    }

    @Test
    public void testToIntStream_List() {
        final int[] expected314159 = { 3, 1, 4, 1, 5, 9 };
        assertArrayEquals(expected314159, Converters.toIntStream("3,1,4,1,5,9").toArray());
        
        final int[] expected7 = { 7 };
        assertArrayEquals(expected7, Converters.toIntStream("7").toArray());
    }
    
    @Test
    public void testToIntStream_Complex() {
        final int[] expected = { 1, 5,6,7, 9, 2,3,4, 8 };
        assertArrayEquals(expected, Converters.toIntStream("1;5-7;9;2-4;8").toArray());
    }
}
