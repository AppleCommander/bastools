package io.github.applecommander.bastools.api.shapes;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

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
        return new String(text.toByteArray());
    }
}
