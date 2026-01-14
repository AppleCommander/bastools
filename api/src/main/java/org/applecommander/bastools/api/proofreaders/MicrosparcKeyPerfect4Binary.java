package org.applecommander.bastools.api.proofreaders;

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
public class MicrosparcKeyPerfect4Binary implements BinaryDataProofReader {
    @Override
    public void addBytes(final int address, final byte... binary) {
        System.out.println(" CODE      ADDR# - ADDR#");
        System.out.println("------     -------------");

        MicrosparcKeyPerfect4Checksum lineChecksum = new MicrosparcKeyPerfect4Checksum();
        final List<Integer> addrs = new ArrayList<>();
        for (int i=0; i<binary.length; i++) {
            addrs.add(address+i);
            lineChecksum.add(Byte.toUnsignedInt(binary[i]));
            if (addrs.size() == 0x50) {
                printAddrs(addrs, lineChecksum.value());
                lineChecksum.reset();
                addrs.clear();
            }
        }
        if (!addrs.isEmpty()) {
            printAddrs(addrs, lineChecksum.value());
        }
        printAddr("PROGRAM TOTAL", binary.length);
    }

    public void printAddrs(List<Integer> addrs, int lineChecksum) {
        int firstAddr = addrs.getFirst();
        int lastAddr = addrs.getLast();
        String text = String.format(" %04X - %04X ", firstAddr, lastAddr);
        printAddr(text, lineChecksum);
    }

    public static void printAddr(String text, int checksum) {
        String fmt = "    %02X";
        if (checksum > 0xffff) {
            fmt = "%06X";
        }
        else if (checksum > 0xff) {
            fmt = "  %04X";
        }
        System.out.printf(fmt, checksum);
        System.out.printf("     %s", text);
        System.out.println();
    }
}
