/*
 * bastools
 * Copyright (C) 2026  Robert Greene
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
package org.applecommander.bastools.api.proofreaders;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.model.Program;

public class ComputeAutomaticProofreader implements ApplesoftInputBufferProofReader {
    private final Configuration config;
    private final Checksum checksum = new Checksum();

    public ComputeAutomaticProofreader(Configuration config) {
        this.config = config;
    }

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public void addProgram(Program program) {
        System.out.println("Compute! Apple Automatic Proofreader, Copyright 1985");
        ApplesoftInputBufferProofReader.super.addProgram(program);
    }

    @Override
    public void addLine(String line) {
        checksum.reset();
        for (int i=line.length()-1; i>=0; i--) {
            char ch = line.charAt(i);
            if (ch != ' ') {
                checksum.add(ch|0x80);
            }
        }

        System.out.printf("%02X | %s\n", checksum.value(), line);
    }

    // For testing purposes; only valid for the line
    protected int getChecksumValue() {
        return checksum.value();
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
    public static class Checksum implements ProofReaderChecksum {
        private int checksum = 0;

        @Override
        public void reset() {
            this.checksum = 0;
        }

        @Override
        public void add(int value) {
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
        }

        @Override
        public int value() {
            return checksum;
        }
    }
}
