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
package org.applecommander.bastools.api.shapes;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ShapeWriteAndReadTests {
    @Parameters(name = "{index}: file= {0}")
    public static Collection<String> data() {
        return Arrays.asList("/mouse-bitmap.st", "/robot-bitmap.st");
    }
    
    @Parameter
    public String filename;
    
    private ShapeExporter textExporter;
    
    @Before
    public void setup() {
        textExporter = ShapeExporter.text().asciiTextBorder().skipEmptyShapes(true).build();
    }
    
    @Test
    public void test() throws IOException {
        ShapeTable stBefore = ShapeGenerator.generate(getClass().getResourceAsStream(filename));
        assertNotNull(stBefore);
        assertFalse(stBefore.shapes.isEmpty());
        
        final String expected = format(stBefore);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stBefore.write(outputStream);
        
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        ShapeTable stAfter = ShapeTable.read(inputStream);
        
        String actual = format(stAfter);
        assertEquals(expected, actual);
    }
    
    public String format(ShapeTable st) throws IOException {
        ByteArrayOutputStream text = new ByteArrayOutputStream();
        textExporter.export(st, text);
        return text.toString();
    }
}
