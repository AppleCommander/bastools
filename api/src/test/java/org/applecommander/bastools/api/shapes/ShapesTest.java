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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class ShapesTest {
    /** 
     * This shape data is taken from the Applesoft BASIC Programmer's Reference Manual (1987), p146.
     */
    public static final byte[] BOX_SAMPLE = { 0x01, 0x00, 0x04, 0x00, 0x12, 0x3F, 0x20, 0x64, 0x2d, 0x15, 0x36, 0x1e, 0x07, 0x00 };
    /** 
     * These shape vectors are taken from the Applesoft BASIC Programmer's Reference Manual (1987), p146.
     */
    public VectorShape drawStandardBoxShape() {
        return new VectorShape()
                .moveDown().moveDown()
                .plotLeft().plotLeft()
                .moveUp().plotUp().plotUp().plotUp()
                .moveRight().plotRight().plotRight().plotRight()
                .moveDown().plotDown().plotDown().plotDown()
                .moveLeft().plotLeft();

    }
    public BitmapShape plotStandardBoxShape() {
        BitmapShape boxShape = new BitmapShape(5, 5);
        for (int i=1; i<=3; i++) {
            boxShape.plot(i, 0);
            boxShape.plot(i, 4);
            boxShape.plot(0, i);
            boxShape.plot(4, i);
        }
        boxShape.origin.setLocation(2, 2);
        return boxShape;
    }

    public ShapeTable readStandardShapeTable() {
        ShapeTable st = ShapeTable.read(BOX_SAMPLE);
        assertNotNull(st);
        assertNotNull(st.shapes);
        assertEquals(1, st.shapes.size());
        return st;
    }
    
    @Test
    public void testWriteStandardShapeTable() throws IOException {
        ShapeTable st = new ShapeTable();
        st.shapes.add(drawStandardBoxShape());
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        st.write(outputStream);
        
        assertArrayEquals(BOX_SAMPLE, outputStream.toByteArray());
    }
    
    @Test
    public void testStandardShapeTableVectors() {
        ShapeTable st = readStandardShapeTable();
        
        VectorShape expected = new VectorShape()
                .moveDown().moveDown()
                .plotLeft().plotLeft()
                .moveUp().plotUp().plotUp().plotUp()
                .moveRight().plotRight().plotRight().plotRight()
                .moveDown().plotDown().plotDown().plotDown()
                .moveLeft().plotLeft();
      
        Shape s = st.shapes.getFirst();
        assertNotNull(s);
        assertEquals(expected.vectors, s.toVector().vectors);
    }
    
    @Test
    public void testStandardShapeTableBitmap() {
        ShapeTable st = readStandardShapeTable();
        
        BitmapShape expected = plotStandardBoxShape();
        
        Shape s = st.shapes.getFirst();
        assertEquals(expected.grid, s.toBitmap().grid);
    }
    
    @Test
    public void testToVectorFromBitmap() {
        BitmapShape bitmapShape = plotStandardBoxShape();
        
        VectorShape vectorShape = bitmapShape.toVector();
        BitmapShape newBitmapShape = vectorShape.toBitmap();

        assertEquals(bitmapShape.grid, newBitmapShape.grid);
    }
    
    @Test
    public void testTextShapeExporterNoBorder() throws IOException {
        ShapeTable st = readStandardShapeTable();
        
        final String expected = """
            .XXX.
            X...X
            X.+.X
            X...X
            .XXX.
            """;
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ShapeExporter exp = ShapeExporter.text().noBorder().build();
        exp.export(st.shapes.getFirst(), outputStream);
        String actual = outputStream.toString();
        
        assertEquals(expected, actual);
    }

    @Test
    public void testTextShapeExporterAsciiBorder() throws IOException {
        ShapeTable st = readStandardShapeTable();
        
        final String expected = """
            +-----+
            |.XXX.|
            |X...X|
            |X.+.X|
            |X...X|
            |.XXX.|
            +-----+
            """;
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ShapeExporter exp = ShapeExporter.text().asciiTextBorder().build();
        exp.export(st.shapes.getFirst(), outputStream);
        String actual = outputStream.toString();
        
        assertEquals(expected, actual);
    }

    @Test
    public void testTextShapeTableExporterNoBorder() throws IOException {
        ShapeTable st = readStandardShapeTable();
        
        // Simulate 4 of these identical shapes by adding 3 more
        st.shapes.add(st.shapes.getFirst());
        st.shapes.add(st.shapes.getFirst());
        st.shapes.add(st.shapes.getFirst());
        
        final String oneExpectedRow = """
            .XXX. .XXX.
            X...X X...X
            X.+.X X.+.X
            X...X X...X
            .XXX. .XXX.
            """;
        String expected = oneExpectedRow + "\n" + oneExpectedRow;
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ShapeExporter exp = ShapeExporter.text().maxWidth(12).noBorder().build();
        exp.export(st, outputStream);
        String actual = outputStream.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void testTextShapeTableExporterAsciiBorder() throws IOException {
        ShapeTable st = readStandardShapeTable();
        
        // Simulate 4 of these identical shapes by adding 3 more
        st.shapes.add(st.shapes.getFirst());
        st.shapes.add(st.shapes.getFirst());
        st.shapes.add(st.shapes.getFirst());
        
        final String divider = "+-----+-----+\n";
        final String oneExpectedRow = divider
                                    + "|.XXX.|.XXX.|\n"
                                    + "|X...X|X...X|\n"
                                    + "|X.+.X|X.+.X|\n"
                                    + "|X...X|X...X|\n"
                                    + "|.XXX.|.XXX.|\n";
        String expected = oneExpectedRow + oneExpectedRow + divider;
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ShapeExporter exp = ShapeExporter.text().maxWidth(12).asciiTextBorder().build();
        exp.export(st, outputStream);
        String actual = outputStream.toString();
        
        assertEquals(expected, actual);
    }
}
