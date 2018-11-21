package io.github.applecommander.bastools.api.utils;

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
