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
 * Perform the MicroSPARC Key Perfect checksum V4.
 * <p/>
 * Partial disassembly; note that some V5 checksum logic is embedded here but not use.
 * Key takeaways:
 * <ul>
 * <li>Program "checksum" is essentially a byte count of number of bytes used in checksum,
 *     but also including next line pointer</li>
 * <li>Checksums ignore whitespace except for Ctrl+D (spaces are ignored)</li>
 * <li>No special REM logic for V4 algorithm</li>
 * <li>Line checksum has low-byte as an EOR, ROL, ADC sequence and the next two bytes end up being a counter
 *     of the number of carry bits that resulted</li>
 * <li>Output skips printing 00 bytes except for the low-value (+2 index)</li>
 * </ul>
 * <pre>
 * DO_CHKSUM_V4    lda   LINE_BYTES_READ
 *                 beq   ]CALC_V4_CHKSUM
 *                 lda   OS_SELECTION
 *                 beq   ]PRODOS
 *                 lda   PD_C4_FILETYPE
 *                 cmp   #$01            ;Integer BASIC?
 *                 beq   ]INT_BASIC
 * ]PRODOS         lda   CHAR_READ
 *                 cmp   #$21            ;'!' or higher (printable char/token)
 *                 bcs   ]CALC_V4_CHKSUM
 *                 cmp   #$04            ;Embedded CTRL+D?
 *                 beq   ]CALC_V4_CHKSUM
 *                 rts
 *
 * ]INT_BASIC      lda   CHAR_READ
 *                 cmp   #$80            ;Int BASIC tokens have high bit OFF
 *                 bcc   ]CALC_V4_CHKSUM
 *                 cmp   #$a1            ;'!' or higher (high bit set)
 *                 bcs   ]CALC_V4_CHKSUM
 *                 cmp   #$84            ;Embedded CTRL+D
 *                 beq   ]CALC_V4_CHKSUM
 *                 rts
 *
 * ]CALC_V4_CHKSUM jsr   CALC_CKSUM_V4_PGM
 *                 lda   TOKEN_FLAG
 *                 cmp   #$02            ;2=REM has been recorded
 *                 beq   ]NOT_REM
 *                 lda   CHAR_READ
 *                 jsr   RUN_V5_CHECKSUM ;Non-REM contents. (V5 only)
 *                 lda   TOKEN_FLAG
 *                 cmp   #$01
 *                 bne   ]NOT_REM
 *                 lda   #$02
 *                 sta   TOKEN_FLAG
 * ]NOT_REM        lda   CHAR_READ
 *                 clc
 *                 eor   CHKSUM_V4_LINE+2
 *                 rol   A
 *                 ldx   #$02
 * ]LOOP           adc   CHKSUM_V4_LINE,x
 *                 sta   CHKSUM_V4_LINE,x
 *                 bcc   ]DONE
 *                 lda   #$00
 *                 dex
 *                 bpl   ]LOOP
 * ]DONE           rts
 *
 * CALC_CKSUM_V4_PGM
 *                 ldx #$02
 * ]LOOP           clc
 *                 lda   #$01
 *                 adc   CHKSUM_V4_PGM,x
 *                 sta   CHKSUM_V4_PGM,x
 *                 bcc   ]DONE
 *                 dex
 *                 bpl   ]LOOP
 * ]DONE           rts
 * </pre>
 * ('@' replaced with ']' since Javadoc uses '@' for other purposes.)
 */
public class MicrosparcKeyPerfect4Applesoft implements ApplesoftTokenizedProofReader {
    private final Configuration config;

    public MicrosparcKeyPerfect4Applesoft(Configuration config) {
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

        System.out.println("Line# - Line#   CODE-4.0");
        System.out.println("-------------   --------");

        MicrosparcKeyPerfect4Checksum checksum = new MicrosparcKeyPerfect4Checksum();
        int programChecksum = 0;    // (Mostly a byte counter)
        final List<Integer> lines = new ArrayList<>();
        while (code.hasRemaining()) {
            int ptr = code.getShort();
            programChecksum+= 2;    // Includes the 0000 end pointer
            if (ptr == 0) break;
            // Line number always gets added to checksum and total
            int b1 = Byte.toUnsignedInt(code.get());
            checksum.add(b1);
            int b2 = Byte.toUnsignedInt(code.get());
            checksum.add(b2);
            programChecksum+= 2;
            lines.add(b2 << 8 | b1);
            // Process tokenized line...
            int ch = 0;
            do {
                ch = Byte.toUnsignedInt(code.get());
                if (ch > 0x20 || ch == 0x04) {
                    programChecksum++;
                    checksum.add(ch);
                }
            } while (ch > 0);

            if (lines.size() == 10) {
                printLines(lines, checksum);
                checksum.reset();
                lines.clear();
            }
        }
        if (!lines.isEmpty()) {
            printLines(lines, checksum);
        }
        printLine("PROGRAM TOTAL", programChecksum);
    }

    public void printLines(List<Integer> lines, MicrosparcKeyPerfect4Checksum checksum) {
        int firstLine = lines.getFirst();
        int lastLine = lines.getLast();
        String text = String.format("%5d - %5d", firstLine, lastLine);
        printLine(text, checksum.value());
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
