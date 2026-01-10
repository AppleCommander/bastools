package org.applecommander.bastools.api.proofreaders;

import org.applecommander.bastools.api.Configuration;

public class NibbleCheckit implements ApplesoftInputBufferProofReader {
    private final Configuration config;
    private final Checksum totalChecksum = new Checksum();
    private final Checksum lineChecksum = new Checksum();

    public NibbleCheckit(Configuration config) {
        this.config = config;
    }

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    /** {@inheritDoc} */
    @Override
    public void addProgramText(String code) {
        System.out.println("Nibble Checkit, Copyright 1988, Microsparc Inc.");
        ApplesoftInputBufferProofReader.super.addProgramText(code);
        System.out.printf("TOTAL: %02X%02X\n", totalChecksum.checksum & 0xff, totalChecksum.checksum >> 8);
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(final String originalLine) {
        // Calculate and output line value
        lineChecksum.reset();

        // The ? => PRINT replacement always occurs, including in strings!
        String line = originalLine.replace("?", "PRINT");

        boolean inQuote = false;
        StringBuilder remLettersSeen = new StringBuilder();
        for (char ch : line.toCharArray()) {
            if (ch == '"') inQuote = !inQuote;
            if (!inQuote && ch == ' ') continue;
            lineChecksum.add(ch|0x80);

            // we only allow R E M from a comment; skip rest of the comment
            remLettersSeen.append(ch);
            if ("REM".contentEquals(remLettersSeen)) {
                break;
            }
            else if ("R".contentEquals(remLettersSeen) || "RE".contentEquals(remLettersSeen)) {
                // Keep them, this may be a comment
            }
            else {
                remLettersSeen = new StringBuilder();
            }
        }

        int cs = ( (lineChecksum.checksum & 0xff) - (lineChecksum.checksum >> 8) ) & 0xff;
        System.out.printf("%02X | %s\n", cs, originalLine);

        // Update program values
        int lineNumber = Integer.parseInt(line.split(" ")[0].trim());
        totalChecksum.add(lineNumber & 0xff);
        totalChecksum.add(lineNumber >> 8);
    }

    public int getLineChecksumValue() {
        return lineChecksum.value();
    }
    public int getTotalChecksumValue() {
        return totalChecksum.value();
    }

    /**
     * Perform Nibble Checkit algorithm. Note that a large part of the assembly is shifting the
     * new value byte into the checksum; if the checksum itself causes a carry in the high byte,
     * an exclusive-or is done with (maybe?) the CCITT CRC polynomial.
     * <pre>
     * 864F- A2 08     L864F      LDX   #$08        ; 8 bits
     * 8651- 0A        L8651      ASL               ; Acc. is value
     * 8652- 26 08                ROL   $08         ; $08/$09 is low/high byte of checksum
     * 8654- 26 09                ROL   $09
     * 8656- 90 0E                BCC   L8666       ; no carry, continue loop
     * 8658- 48                   PHA               ; save current value
     * 8659- A5 08                LDA   $08
     * 865B- 49 21                EOR   #$21        ; low byte of $1021
     * 865D- 85 08                STA   $08
     * 865F- A5 09                LDA   $09
     * 8661- 49 10                EOR   #$10        ; high byte of $1021
     * 8663- 85 09                STA   $09
     * 8665- 68                   PLA               ; restore current value
     * 8666- CA        L8666      DEX
     * 8667- D0 E8                BNE   L8651       ; loop through all 8 bits
     * 8669- 60                   RTS
     * </pre>
     * In this implementation, we just mash the value and the checksum together, so the
     * resulting number is <code>0x00CCCCVV</code>. Then the intermediate carry bits aren't
     * a concern, and we simply need to detect when we overflow three bytes and then do the
     * XOR. The result is the middle two bytes where the checksum resides. (Note that the
     * <code>0x1021</code> was shifted by a byte as well.)
     */
    public static class Checksum implements ProofReaderChecksum {
        private int checksum = 0;
        @Override
        public void reset() {
            this.checksum = 0;
        }
        @Override
        public void add(int value) {
            assert value >= 0 && value <= 0xff;
            int work = (checksum << 8) | value;
            for (int i=0; i<8; i++) {
                work <<= 1;
                // Note: If we run into negative issues somehow, this could also be "((work & 0xff000000) != 0)".
                if (work > 0xffffff) {
                    work &= 0xffffff;
                    work ^= 0x102100;
                }
            }
            checksum = work >> 8;
        }
        @Override
        public int value() {
            return checksum;
        }
    }
}
