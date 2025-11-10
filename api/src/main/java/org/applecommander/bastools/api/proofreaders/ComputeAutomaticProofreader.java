package org.applecommander.bastools.api.proofreaders;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.model.Line;
import org.applecommander.bastools.api.model.Program;

public class ComputeAutomaticProofreader extends LineOrientedProofReader {
    public ComputeAutomaticProofreader(Configuration config) {
        super(config);
    }

    @Override
    public Program visit(Program program) {
        System.out.println("Compute! Apple Automatic Proofreader, Copyright 1985");
        return super.visit(program);
    }

    @Override
    public Line visit(Line line) {
        String text = toString(line);

        int checksum = 0;
        for (int i=text.length()-1; i>=0; i--) {
            char ch = text.charAt(i);
            if (ch != ' ') {
                checksum = proofreader(checksum, ch|0x80);
            }
        }

        System.out.printf("%02X|%s\n", checksum, text);
        return line;
    }

    /**
     * Perform the Compute! Apple Automatic Proofreader calculation. Note that the checksum is performed
     * in reverse order on the input buffer, skipping all spaces.
     * <pre>
     * 0314- 68                   PLA              ; add to cksum
     * 0315- 0A                   ASL
     * 0316- 7D FF 01             ADC   $01FF,X
     * 0319- 69 00                ADC   #$00
     * 031B- 48                   PHA
     * </pre>
     * Generalized algorithm: <code>cksum = (cksum << 1) + (char|$80) + carry</code>
     */
    public static int proofreader(int checksum, int value) {
        assert value >= 0 && value <= 0xff;
        checksum <<= 1;
        // Handle carry from ASL
        if (checksum > 0xff) {
            checksum++;
            checksum &= 0xff;
        }
        // Handle carry from first ADC
        checksum += value;
        if (checksum > 0xff) {
            checksum++;
            checksum &= 0xff;
        }
        return checksum;
    }
}
