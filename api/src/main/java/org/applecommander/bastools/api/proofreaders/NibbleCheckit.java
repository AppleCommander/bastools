package org.applecommander.bastools.api.proofreaders;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.model.Line;
import org.applecommander.bastools.api.model.Program;
import org.applecommander.bastools.api.model.Statement;
import org.applecommander.bastools.api.model.Token;

public class NibbleCheckit extends LineOrientedProofReader {
    private int totalChecksum;
    private int lineChecksum;

    public NibbleCheckit(Configuration config) {
        super(config);
    }

    @Override
    public Program visit(Program program) {
        System.out.println("Nibble Checkit, Copyright 1988, Microsparc Inc.");
        try {
            return super.visit(program);
        } finally {
            System.out.printf("TOTAL: %02X%02X\n", totalChecksum & 0xff, totalChecksum >> 8);
        }
    }

    @Override
    public Line visit(Line line) {
        // Calculate and output line value
        lineChecksum = 0;
        for (char ch : Integer.toString(line.lineNumber).toCharArray()) {
            lineChecksum = checkit(lineChecksum, ch|0x80);
        }
        boolean first = true;
        for (Statement statement : line.statements) {
            if (!first) {
                // We need to synthesize the colons
                lineChecksum = checkit(lineChecksum, ':'|0x80);
            }
            super.visit(statement);
            first = false;
        }
        int cs = ( (lineChecksum & 0xff) - (lineChecksum >> 8) ) & 0xff;
        System.out.printf("%02X|%s\n", cs, toString(line));

        // Update program values
        totalChecksum = checkit(totalChecksum, line.lineNumber & 0xff);
        totalChecksum = checkit(totalChecksum, line.lineNumber >> 8);
        return line;
    }

    @Override
    public Token visit(Token token) {
        String value = switch (token.type()) {
            case EOL, DIRECTIVE -> "";
            case DATA, SYNTAX, IDENT -> token.text();
            case COMMENT -> "REM";
            case KEYWORD -> token.keyword().text;
            case NUMBER -> {
                if (token.text() != null) {
                    yield token.text();
                }
                yield config.numberToString(token);
            }
            case STRING -> String.format("\"%s\"", token.text());
        };
        for (char ch : value.toCharArray()) {
            lineChecksum = checkit(lineChecksum, ch|0x80);
        }
        return token;
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
    public static int checkit(int checksum, int value) {
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
        return work >> 8;
    }
}
