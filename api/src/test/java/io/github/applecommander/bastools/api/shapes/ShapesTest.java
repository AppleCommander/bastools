package io.github.applecommander.bastools.api.shapes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class ShapesTest {
    /** 
     * This shape is taken from the Applesoft BASIC Programmer's Reference Manual (1987), p146.
     */
    public ShapeTable readStandardShapeTable() {
        final byte[] sample = { 0x01, 0x00, 0x04, 0x00, 0x12, 0x3F, 0x20, 0x64, 0x2d, 0x15, 0x36, 0x1e, 0x07, 0x00 };
        ShapeTable st = ShapeTable.read(sample);
        assertNotNull(st);
        assertNotNull(st.shapes);
        assertEquals(1, st.shapes.size());
        return st;
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
      
        Shape s = st.shapes.get(0);
        assertNotNull(s);
        assertEquals(expected.vectors, s.toVector().vectors);
    }
    
    @Test
    public void testStandardShapeTableBitmap() {
        ShapeTable st = readStandardShapeTable();
        
        BitmapShape expected = new BitmapShape(5, 5);
        for (int i=1; i<=3; i++) {
            expected.plot(i, 0);
            expected.plot(i, 4);
            expected.plot(0, i);
            expected.plot(4, i);
        }
        
        Shape s = st.shapes.get(0);
        assertEquals(expected.grid, s.toBitmap().grid);
    }
    
    @Test
    public void testTextShapeExporterNoBorder() throws IOException {
        ShapeTable st = readStandardShapeTable();
        
        final String expected = ".XXX.\n"
                              + "X...X\n"
                              + "X.+.X\n"
                              + "X...X\n"
                              + ".XXX.\n";
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ShapeExporter exp = ShapeExporter.text().noBorder().build();
        exp.export(st.shapes.get(0), outputStream);
        String actual = new String(outputStream.toByteArray());
        
        assertEquals(expected, actual);
    }

    @Test
    public void testTextShapeExporterAsciiBorder() throws IOException {
        ShapeTable st = readStandardShapeTable();
        
        final String expected = "+-----+\n"
                              + "|.XXX.|\n"
                              + "|X...X|\n"
                              + "|X.+.X|\n"
                              + "|X...X|\n"
                              + "|.XXX.|\n"
                              + "+-----+\n";
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ShapeExporter exp = ShapeExporter.text().asciiTextBorder().build();
        exp.export(st.shapes.get(0), outputStream);
        String actual = new String(outputStream.toByteArray());
        
        assertEquals(expected, actual);
    }

    @Test
    public void testTextShapeTableExporterNoBorder() throws IOException {
        ShapeTable st = readStandardShapeTable();
        
        // Simulate 4 of these identical shapes by adding 3 more
        st.shapes.add(st.shapes.get(0));
        st.shapes.add(st.shapes.get(0));
        st.shapes.add(st.shapes.get(0));
        
        final String oneExpectedRow = ".XXX. .XXX.\n"
                                    + "X...X X...X\n"
                                    + "X.+.X X.+.X\n"
                                    + "X...X X...X\n"
                                    + ".XXX. .XXX.\n";
        String expected = oneExpectedRow + "\n" + oneExpectedRow;
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ShapeExporter exp = ShapeExporter.text().maxWidth(12).noBorder().build();
        exp.export(st, outputStream);
        String actual = new String(outputStream.toByteArray());

        assertEquals(expected, actual);
    }

    @Test
    public void testTextShapeTableExporterAsciiBorder() throws IOException {
        ShapeTable st = readStandardShapeTable();
        
        // Simulate 4 of these identical shapes by adding 3 more
        st.shapes.add(st.shapes.get(0));
        st.shapes.add(st.shapes.get(0));
        st.shapes.add(st.shapes.get(0));
        
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
        String actual = new String(outputStream.toByteArray());
        
        assertEquals(expected, actual);
    }
}
