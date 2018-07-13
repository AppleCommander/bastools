package io.github.applecommander.bastools.api.code;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;

import org.junit.Test;

import io.github.applecommander.bastools.api.model.ApplesoftKeyword;

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
