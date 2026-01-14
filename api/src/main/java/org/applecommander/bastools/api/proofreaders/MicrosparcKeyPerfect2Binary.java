package org.applecommander.bastools.api.proofreaders;

import java.util.ArrayList;
import java.util.List;

public class MicrosparcKeyPerfect2Binary implements BinaryDataProofReader {
    @Override
    public void addBytes(final int address, final byte... binary) {
        System.out.println(" CODE      ADDR# - ADDR#");
        System.out.println("------     -------------");

        int lineChecksum = 0;       // Mostly a sum
        final List<Integer> addrs = new ArrayList<>();
        for (int i=0; i<binary.length; i++) {
            addrs.add(address+i);
            lineChecksum+= Byte.toUnsignedInt(binary[i]);
            if (addrs.size() == 0x40) {
                printAddrs(addrs, lineChecksum);
                lineChecksum = 0;
                addrs.clear();
            }
        }
        if (!addrs.isEmpty()) {
            printAddrs(addrs, lineChecksum);
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
