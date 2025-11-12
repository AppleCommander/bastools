package org.applecommander.bastools.api.proofreaders;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.Visitor;
import org.applecommander.bastools.api.model.Program;
import org.applecommander.bastools.api.visitors.ByteVisitor;

public class NibbleAppleChecker implements Visitor {
    private final Configuration config;
    private final ByteVisitor byteVisitor;

    public NibbleAppleChecker(Configuration config) {
        this.config = config;
        this.byteVisitor = new ByteVisitor(config);
    }

    @Override
    public Program visit(Program program) {
        byteVisitor.visit(program);
        byte[] code = byteVisitor.getBytes();
        Checksum checksum = new Checksum();
        checksum.compute(code, config.startAddress);

        System.out.printf("On: %s\n", config.sourceFile.getName());
        System.out.println("Type: A");
        System.out.println();
        System.out.printf("Length: %04X\n", checksum.getLength());
        System.out.printf("Checksum: %02X\n", checksum.getChecksum());
        return program;
    }

    public static class Checksum {
        private int length;
        private int checksum;

        public int getLength() {
            return length;
        }
        public int getChecksum() {
            return checksum;
        }

        public void compute(byte[] code, int startAddress) {
            // Length is less 2 for Applesoft
            length = code.length+1 - 2;
            // Zero out the next line links
            int nextAddr = startAddress;
            while (nextAddr >= 0 && nextAddr < code.length) {
                nextAddr = (code[nextAddr+1]<<8 | code[nextAddr]) - startAddress;
                code[nextAddr] = 0;
                code[nextAddr+1] = 0;
            }
            // Compute checksum
            this.checksum = 0;
            for (int i=0; i<code.length; i++) {
                int acc = Byte.toUnsignedInt(code[i]);
                if (acc >= 0x21 || acc == 0x04) {
                    this.checksum = checksum(this.checksum, acc);
                }
                else {
                    // We skip _and_ reduce the length by 1
                    this.length--;
                }
            }
        }
    }

    /**
     * Compute a single iteration of the checksum. Broken out for testing purposes.
     * <pre>
     * SUMIT  CLC
     *        EOR CKCD
     *        ROL A
     *        ADC CKCD
     *        ADC #0
     *        STA CKCD
     * </pre>
     */
    public static int checksum(int checksum, int acc) {
        // EOR CKCD
        acc ^= checksum;
        // ROL A
        acc <<= 1;
        // ADC CKCD
        if (acc >= 0xff) {
            checksum++;
            acc &= 0xff;
        }
        acc += checksum;
        // ADC #0
        if (acc > 0xff) {
            acc++;
            acc &= 0xff;
        }
        // STA CKCD
        return acc;
    }
}
