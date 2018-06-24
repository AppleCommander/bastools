package io.github.applecommander.bastools.api.shapes;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

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
        assertEquals("label-bitmap", st.shapes.get(0).toBitmap().label);
    }

    public void assertShapeIsBox(ShapeTable st) throws IOException {
        assertNotNull(st);
        assertEquals(1, st.shapes.size());
        
        final String expected = "+-----+\n"
                              + "|.XXX.|\n"
                              + "|X...X|\n"
                              + "|X.+.X|\n"
                              + "|X...X|\n"
                              + "|.XXX.|\n"
                              + "+-----+\n";

        assertShapeMatches(expected, st.shapes.get(0));
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
      
        Shape shape = st.shapes.get(0);
        assertNotNull(shape);
        assertTrue(shape instanceof VectorShape);
        VectorShape vshape = shape.toVector();
        assertEquals(expected.vectors, vshape.vectors);
        assertEquals(expected.label, vshape.label);
    }
    
    @Test
    public void testMouseShape() throws IOException {
        final String mouse = "+--------------+\n" 
                           + "|..........*X..|\n"  
                           + "|....XXXX.XX...|\n"  
                           + "|...XXXXXXXX...|\n"
                           + "|.XXXXXXXXXXX..|\n"
                           + "|XX.XXXXXXX.XX.|\n"
                           + "|X...XXXXXXXXXX|\n"
                           + "|XX............|\n"
                           + "|.XXX.XX.......|\n"
                           + "|...XXX........|\n"
                           + "+--------------+\n";

        ShapeTable st = ShapeGenerator.generate(getClass().getResourceAsStream("/mouse-bitmap.st"));
        assertNotNull(st);
        assertEquals(1, st.shapes.size());

        // Verify we read the shape correctly...
        Shape shape = st.shapes.get(0);
        assertNotNull(shape);
        assertShapeMatches(mouse, shape);
        
        // Run vector transform to be certain we're ok
        Shape vectorShape = shape.toVector();
        assertNotNull(vectorShape);
        assertShapeMatches(mouse, vectorShape);
    }
    
    @Test
    public void testRobotShape() throws IOException {
        final String robot = "+-------------+\n"
                           + "|....XXXXX...+|\n" 
                           + "|XXXXX...XX...|\n" 
                           + "|....XXXXX....|\n" 
                           + "|.............|\n" 
                           + "|..XX..XXX....|\n" 
                           + "|...XX.XXX....|\n" 
                           + "|...XX.XXXX...|\n" 
                           + "|..XX.XXXXX...|\n" 
                           + "|....XXXXXX...|\n" 
                           + "|.XXXXXXXXXXX.|\n" 
                           + "|XX.........XX|\n" 
                           + "|XX.........XX|\n" 
                           + "|.XXXXXXXXXXX.|\n" 
                           + "+-------------+\n";

        ShapeTable st = ShapeGenerator.generate(getClass().getResourceAsStream("/robot-bitmap.st"));
        assertNotNull(st);
        assertEquals(1, st.shapes.size());

        // Verify we read the shape correctly...
        Shape shape = st.shapes.get(0);
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
        String actual = new String(outputStream.toByteArray());
        assertEquals(expected, actual);
    }
}
