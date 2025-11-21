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
 * Perform the MicroSPARC Key Perfect checksum V2.
 */
public class MicrosparcKeyPerfect2 implements Visitor {
    private final Configuration config;
    private final ByteVisitor byteVisitor;

    public MicrosparcKeyPerfect2(Configuration config) {
        this.config = config;
        this.byteVisitor = new ByteVisitor(config);
    }

    @Override
    public Program visit(Program program) {
        byteVisitor.visit(program);
        ByteBuffer code = ByteBuffer.wrap(byteVisitor.getBytes());
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

        return program;
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
