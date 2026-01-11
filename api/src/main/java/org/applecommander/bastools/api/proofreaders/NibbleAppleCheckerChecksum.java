package org.applecommander.bastools.api.proofreaders;

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
public class NibbleAppleCheckerChecksum implements ProofReaderChecksum {
    private int checksum;

    @Override
    public void reset() {
        this.checksum = 0;
    }

    @Override
    public void add(int acc) {
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
        checksum = acc;
    }

    @Override
    public int value() {
        return this.checksum;
    }
}
