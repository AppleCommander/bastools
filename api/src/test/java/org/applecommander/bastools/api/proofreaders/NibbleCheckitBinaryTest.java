package org.applecommander.bastools.api.proofreaders;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class NibbleCheckitBinaryTest {
    @Test
    public void testExtermShapes() {
        // Nibble, June 1989, page 53
        final String code = """
            6100:04 00 0a 00 29 00 4d 00
            6108:73 00 49 29 15 3e 3c 37
            6110:35 3e 3f 3e 2e 4d 2c 24
            6118:15 2d 2e 3e 96 3a 3c fe
            6120:3b 67 25 2c 25 15 3e 3f
            6128:00 92 32 2e 24 24 25 2d
            6130:c1 2d 15 35 35 15 36 27
            6138:3c 3c c1 3f 5f f7 2e 2d
            6140:ac f5 3f 2e 35 3f 17 2d
            6148:2d 3e 3f 3f 00 49 09 ad
            6150:3f bf 6d 29 ad ff 3b 3f
            6158:3f 17 6d 29 4d 6d 3a df
            6160:3f df bf 2d 2d 6d 29 f5
            6168:1b ff 1b bf 6d 29 f5 3b
            6170:3f 3f 00 35 35 35 35 35
            6178:35 35 35 35 35 25 c1 c1
            6180:c1 c1 c1 c1 c1 c1 c1 37
            6188:37 37 37 f7 3a 3e 3e 3e
            6190:3e 06 00
            """.toUpperCase();  // yes, being lazy
        final int[] expectedLine = {
            0x59, 0x5c, 0xa5, 0x24, 0xa6, 0x8a, 0xf9, 0x52,
            0x32, 0x6c, 0xa5, 0x2c, 0x35, 0xf4, 0xb0, 0x03,
            0xb0, 0xdf, 0x11
        };
        final int expectedTotal = 0x1dd8;

        check(code, expectedLine, expectedTotal);
    }

    public void check(final String code, final int[] expectedLine, final int expectedTotal) {
        NibbleCheckitBinary proofreader = new NibbleCheckitBinary();
        List<String> lines = code.lines().toList();
        assertEquals("lines are expected length", expectedLine.length, lines.size());
        for (int i=0; i< lines.size(); i++) {
            String line = lines.get(i);
            proofreader.addLine(line);
            int expectedChecksum = expectedLine[i];
            assertEquals("line checksum", expectedChecksum, proofreader.getLineChecksumValue());
        }
        assertEquals("total checksum", expectedTotal, proofreader.getTotalChecksumValue());
    }
}
