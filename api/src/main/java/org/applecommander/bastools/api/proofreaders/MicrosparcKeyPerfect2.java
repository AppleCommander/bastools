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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Perform the MicroSPARC Key Perfect checksum V2.
 * <p/>
 * This code replicates the KP 2.0 checksum. The general application setup
 * is the same as KP 4.0, so that was used as a basis for locating the checksum.
 * <pre>
 * LINE CHECKSUM:
 * 16E7-   20 00 17    JSR   $1700     ; program checksum
 * 16EA-   AD 2A 17    LDA   $172A     ; last byte read from program (other code skips
 *                                     ; first line pointer, but includes line number and
 *                                     ; all program bytes)
 * 16ED-   F0 10       BEQ   $16FF     ; skips all $00 values
 * 16EF-   A2 02       LDX   #$02      ; add three byte integer
 * 16F1-   18          CLC
 * 16F2-   7D 36 17    ADC   $1736,X
 * 16F5-   9D 36 17    STA   $1736,X
 * 16F8-   90 05       BCC   $16FF     ; no carry, so we're done
 * 16FA-   A9 01       LDA   #$01
 * 16FC-   CA          DEX
 * 16FD-   10 F2       BPL   $16F1
 * 16FF-   60          RTS
 *
 * PROGRAM CHECKSUM:
 * 1700-   A2 02       LDX   #$02      ; note every byte (including first line pointer)
 * 1702-   18          CLC
 * 1703-   A9 01       LDA   #$01      ; simple increment
 * 1705-   7D 32 17    ADC   $1732,X
 * 1708-   9D 32 17    STA   $1732,X
 * 170B-   90 03       BCC   $1710
 * 170D-   CA          DEX
 * 170E-   10 F2       BPL   $1702
 * 1710-   60          RTS
 * </pre>
 */
public class MicrosparcKeyPerfect2 implements ApplesoftTokenizedProofReader {
    private final Configuration config;

    public MicrosparcKeyPerfect2(Configuration config) {
        this.config = config;
    }

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    @Override
    public void addBytes(byte... tokenizedProgram) {
        ByteBuffer code = ByteBuffer.wrap(tokenizedProgram);
        code.order(ByteOrder.LITTLE_ENDIAN);

        System.out.println("Line# - Line#   CODE-2.0");
        System.out.println("-------------   --------");

        int lineChecksum = 0;       // Mostly a sum
        int programChecksum = 0;    // Mostly a byte counter
        final List<Integer> lines = new ArrayList<>();
        while (code.hasRemaining()) {
            int ptr = code.getShort();
            programChecksum+= 2;    // Includes the 0000 end pointer
            if (ptr == 0) break;
            // Line number always gets added to checksum and total
            int b1 = Byte.toUnsignedInt(code.get());
            lineChecksum += b1;
            int b2 = Byte.toUnsignedInt(code.get());
            lineChecksum += b2;
            programChecksum+= 2;
            lines.add(b2 << 8 | b1);
            // Process tokenized line...
            int ch = 0;
            do {
                ch = Byte.toUnsignedInt(code.get());
                programChecksum++;
                if (ch > 0) {
                    lineChecksum += ch;
                }
            } while (ch > 0);

            if (lines.size() == 10) {
                printLines(lines, lineChecksum);
                lineChecksum = 0;
                lines.clear();
            }
        }
        if (!lines.isEmpty()) {
            printLines(lines, lineChecksum);
        }
        printLine("PROGRAM TOTAL", programChecksum);
    }

    public void printLines(List<Integer> lines, int lineChecksum) {
        int firstLine = lines.getFirst();
        int lastLine = lines.getLast();
        String text = String.format("%5d - %5d", firstLine, lastLine);
        printLine(text, lineChecksum);
    }

    public static void printLine(String text, int checksum) {
        System.out.printf("%-13.13s     ", text);
        String fmt = "    %02X";
        if (checksum > 0xffff) {
            fmt = "%06X";
        }
        else if (checksum > 0xff) {
            fmt = "  %04X";
        }
        System.out.printf(fmt, checksum);
        System.out.println();
    }
}
