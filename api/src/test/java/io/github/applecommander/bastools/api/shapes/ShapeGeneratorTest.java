package io.github.applecommander.bastools.api.shapes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

public class ShapeGeneratorTest {
    @Test
    public void generateBoxShortformTest() throws IOException {
        ShapeTable st = ShapeGenerator.generate(getClass().getResourceAsStream("/box-shortform.st"));
        assertShapeIsBox(st);
        assertShapeBoxVectors(st);
    }

    @Test
    public void generateBoxLongformTest() throws IOException {
        ShapeTable st = ShapeGenerator.generate(getClass().getResourceAsStream("/box-longform.st"));
        assertShapeIsBox(st);
        assertShapeBoxVectors(st);
    }

    @Test
    public void generateBoxBitmapTest() throws IOException {
        ShapeTable st = ShapeGenerator.generate(getClass().getResourceAsStream("/box-bitmap.st"));
        assertShapeIsBox(st);
        // Unable to test vectors for bitmaps
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

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ShapeExporter exp = ShapeExporter.text().asciiTextBorder().build();
        exp.export(st.shapes.get(0), outputStream);
        String actual = new String(outputStream.toByteArray());
        
        assertEquals(expected, actual);
    }
    
    public void assertShapeBoxVectors(ShapeTable st) {
        assertNotNull(st);
        assertEquals(1, st.shapes.size());
        
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
}
