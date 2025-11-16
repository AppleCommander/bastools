package org.applecommander.bastools.api.proofreaders;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.Visitor;
import org.applecommander.bastools.api.model.Program;
import org.applecommander.bastools.api.visitors.ByteVisitor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Perform the MicroSPARC Key Perfect checksum V4.
 * <p/>
 * Partial disassembly; note that some V5 checksum logic is embedded here but not use.
 * Key takeaways:
 * <li>
 * <ul>Program "checksum" is essentially a byte count of number of bytes used in checksum,
 *     but also including next line pointer</ul>
 * <ul>Checksums ignore whitespace except for Ctrl+D (spaces are ignored)</ul>
 * <ul>No special REM logic for V4 algorithm</ul>
 * <ul>Line checksum has low-byte as an EOR, ROL, ADC sequence and the next two bytes end up being a counter
 *     of the number of carry bits that resulted</ul>
 * <ul>Output skips printing 00 bytes except for the low-value (+2 index)</ul>
 * </li>
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
 *                 jsr   RUN_CHECKSUM    ;Non-REM contents. (V5 only)
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
public class MicrosparcKeyPerfect4 implements Visitor {
    private final Configuration config;
    private final ByteVisitor byteVisitor;

    public MicrosparcKeyPerfect4(Configuration config) {
        this.config = config;
        this.byteVisitor = new ByteVisitor(config);
    }

    @Override
    public Program visit(Program program) {
        byteVisitor.visit(program);
        ByteBuffer code = ByteBuffer.wrap(byteVisitor.getBytes());
        code.order(ByteOrder.LITTLE_ENDIAN);

        System.out.println("Line# - Line#   CODE-4.0");
        System.out.println("-------------   --------");

        Checksum checksum = new Checksum();
        int programChecksum = 0;    // (Mostly a byte counter)
        final List<Integer> lines = new ArrayList<>();
        while (code.hasRemaining()) {
            int ptr = code.getShort();
            programChecksum+= 2;    // Includes the 0000 end pointer
            if (ptr == 0) break;
            // Line number always gets added to checksum and total
            int b1 = Byte.toUnsignedInt(code.get());
            checksum.addByte(b1);
            int b2 = Byte.toUnsignedInt(code.get());
            checksum.addByte(b2);
            programChecksum+= 2;
            lines.add(b2 << 8 | b1);
            // Process tokenized line...
            int ch = 0;
            do {
                ch = Byte.toUnsignedInt(code.get());
                if (ch > 0x20 || ch == 0x04) {
                    programChecksum++;
                    checksum.addByte(ch);
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

        return program;
    }

    public void printLines(List<Integer> lines, Checksum checksum) {
        int firstLine = lines.getFirst();
        int lastLine = lines.getLast();
        String text = String.format("%5d - %5d", firstLine, lastLine);
        printLine(text, checksum.getChecksum());
    }

    public static void printLine(String text, int checksum) {
        System.out.printf("%-13.13s     ", text);
        String fmt = "    %02X";
        if (checksum > 0xffff) {
            fmt = "$06X";
        }
        else if (checksum > 0xff) {
            fmt = "  %04X";
        }
        System.out.printf(fmt, checksum);
        System.out.println();
    }

    public static class Checksum {
        private int lineChecksum = 0;       // low byte with has EOR, ROL, ADC
        private int lineCounter = 0;        // high 2 bytes that just ADC on carry

        public int getChecksum() {
            return lineCounter << 8 | lineChecksum;
        }

        public void reset() {
            lineChecksum = 0;
            lineCounter = 0;
        }

        public void addByte(int code) {
            int acc = lineChecksum ^ code;
            acc <<= 1;
            if (acc > 0xff) {
                acc &= 0xff;
                lineChecksum++;
            }
            lineChecksum += acc;
            if (lineChecksum > 0xff) {
                lineChecksum &= 0xff;
                lineCounter++;
            }
        }
    }
}
