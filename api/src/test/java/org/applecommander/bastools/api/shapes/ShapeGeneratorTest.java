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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class ShapeGeneratorTest {
    @Test
    public void generateBoxShortformTest() throws IOException {
        ShapeTable st = ShapeGenerator.generate(getClass().getResourceAsStream("/box-shortform.st"));
        assertShapeIsBox(st);
        assertShapeBoxVectors(st, "label-short");
    }

    @Test
    public void generateBoxLongformTest() throws IOException {
        ShapeTable st = ShapeGenerator.generate(getClass().getResourceAsStream("/box-longform.st"));
        assertShapeIsBox(st);
        assertShapeBoxVectors(st, "label-long");
    }

    @Test
    public void generateBoxBitmapTest() throws IOException {
        ShapeTable st = ShapeGenerator.generate(getClass().getResourceAsStream("/box-bitmap.st"));
        assertShapeIsBox(st);
        // Unable to test vectors for bitmaps
        assertEquals("label-bitmap", st.shapes.getFirst().toBitmap().label);
    }

    public void assertShapeIsBox(ShapeTable st) throws IOException {
        assertNotNull(st);
        assertEquals(1, st.shapes.size());
        
        final String expected = """
            +-----+
            |.XXX.|
            |X...X|
            |X.+.X|
            |X...X|
            |.XXX.|
            +-----+
            """;

        assertShapeMatches(expected, st.shapes.getFirst());
    }
    
    public void assertShapeBoxVectors(ShapeTable st, String label) {
        assertNotNull(st);
        assertEquals(1, st.shapes.size());
        
        VectorShape expected = new VectorShape(label)
                .moveDown().moveDown()
                .plotLeft().plotLeft()
                .moveUp().plotUp().plotUp().plotUp()
                .moveRight().plotRight().plotRight().plotRight()
                .moveDown().plotDown().plotDown().plotDown()
                .moveLeft().plotLeft();
      
        Shape shape = st.shapes.getFirst();
        assertNotNull(shape);
        assertTrue(shape instanceof VectorShape);
        VectorShape vshape = shape.toVector();
        assertEquals(expected.vectors, vshape.vectors);
        assertEquals(expected.label, vshape.label);
    }
    
    @Test
    public void testMouseShape() throws IOException {
        final String mouse = """
            +--------------+
            |..........*X..|
            |....XXXX.XX...|
            |...XXXXXXXX...|
            |.XXXXXXXXXXX..|
            |XX.XXXXXXX.XX.|
            |X...XXXXXXXXXX|
            |XX............|
            |.XXX.XX.......|
            |...XXX........|
            +--------------+
            """;

        ShapeTable st = ShapeGenerator.generate(getClass().getResourceAsStream("/mouse-bitmap.st"));
        assertNotNull(st);
        assertEquals(1, st.shapes.size());

        // Verify we read the shape correctly...
        Shape shape = st.shapes.getFirst();
        assertNotNull(shape);
        assertShapeMatches(mouse, shape);
        
        // Run vector transform to be certain we're ok
        Shape vectorShape = shape.toVector();
        assertNotNull(vectorShape);
        assertShapeMatches(mouse, vectorShape);
    }
    
    @Test
    public void testRobotShape() throws IOException {
        final String robot = """
            +-------------+
            |....XXXXX...+|
            |XXXXX...XX...|
            |....XXXXX....|
            |.............|
            |..XX..XXX....|
            |...XX.XXX....|
            |...XX.XXXX...|
            |..XX.XXXXX...|
            |....XXXXXX...|
            |.XXXXXXXXXXX.|
            |XX.........XX|
            |XX.........XX|
            |.XXXXXXXXXXX.|
            +-------------+
            """;

        ShapeTable st = ShapeGenerator.generate(getClass().getResourceAsStream("/robot-bitmap.st"));
        assertNotNull(st);
        assertEquals(1, st.shapes.size());

        // Verify we read the shape correctly...
        Shape shape = st.shapes.getFirst();
        assertNotNull(shape);
        assertShapeMatches(robot, shape);
        
        // Run vector transform to be certain we're ok
        Shape vectorShape = shape.toVector();
        assertNotNull(vectorShape);
        assertShapeMatches(robot, vectorShape);
    }
    
    public void assertShapeMatches(final String expected, Shape shape) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ShapeExporter exp = ShapeExporter.text().asciiTextBorder().build();
        exp.export(shape, outputStream);
        String actual = outputStream.toString();
        assertEquals(expected, actual);
    }
}
