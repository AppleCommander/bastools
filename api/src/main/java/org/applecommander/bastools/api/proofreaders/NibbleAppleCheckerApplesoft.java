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

public class NibbleAppleCheckerApplesoft implements ApplesoftTokenizedProofReader {
    private final Configuration config;
    private int length;
    private final NibbleAppleCheckerChecksum checksum = new NibbleAppleCheckerChecksum();

    public NibbleAppleCheckerApplesoft(Configuration config) {
        this.config = config;
    }

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public void addProgram(Program program) {
        ApplesoftTokenizedProofReader.super.addProgram(program);

        System.out.printf("On: %s\n", config.sourceFile.getName());
        System.out.println("Type: A");
        System.out.println();
        System.out.printf("Length: %04X\n", length);
        System.out.printf("Checksum: %02X\n", checksum.value());
    }

    @Override
    public void addBytes(byte... tokenizedProgram) {
        // Length is less 2 for Applesoft
        length = tokenizedProgram.length+1 - 2;
        // Zero out the next line links
        int nextAddr = config.startAddress;
        while (nextAddr >= 0 && nextAddr < tokenizedProgram.length) {
            nextAddr = (tokenizedProgram[nextAddr+1]<<8 | tokenizedProgram[nextAddr]) - config.startAddress;
            tokenizedProgram[nextAddr] = 0;
            tokenizedProgram[nextAddr+1] = 0;
        }
        // Compute checksum
        checksum.reset();
        for (byte b : tokenizedProgram) {
            int acc = Byte.toUnsignedInt(b);
            if (acc >= 0x21 || acc == 0x04) {
                checksum.add(acc);
            } else {
                // We skip _and_ reduce the length by 1
                this.length--;
            }
        }
    }

    public int getLength() {
        return length;
    }
    public int getChecksumValue() {
        return checksum.value();
    }
}
