package org.applecommander.bastools.api.proofreaders;

/**
 * Perform Nibble Checkit algorithm. Note that a large part of the assembly is shifting the
 * new value byte into the checksum; if the checksum itself causes a carry in the high byte,
 * an exclusive-or is done with (maybe?) the CCITT CRC polynomial.
 * <pre>
 * 864F- A2 08     L864F      LDX   #$08        ; 8 bits
 * 8651- 0A        L8651      ASL               ; Acc. is value
 * 8652- 26 08                ROL   $08         ; $08/$09 is low/high byte of checksum
 * 8654- 26 09                ROL   $09
 * 8656- 90 0E                BCC   L8666       ; no carry, continue loop
 * 8658- 48                   PHA               ; save current value
 * 8659- A5 08                LDA   $08
 * 865B- 49 21                EOR   #$21        ; low byte of $1021
 * 865D- 85 08                STA   $08
 * 865F- A5 09                LDA   $09
 * 8661- 49 10                EOR   #$10        ; high byte of $1021
 * 8663- 85 09                STA   $09
 * 8665- 68                   PLA               ; restore current value
 * 8666- CA        L8666      DEX
 * 8667- D0 E8                BNE   L8651       ; loop through all 8 bits
 * 8669- 60                   RTS
 * </pre>
 * In this implementation, we just mash the value and the checksum together, so the
 * resulting number is <code>0x00CCCCVV</code>. Then the intermediate carry bits aren't
 * a concern, and we simply need to detect when we overflow three bytes and then do the
 * XOR. The result is the middle two bytes where the checksum resides. (Note that the
 * <code>0x1021</code> was shifted by a byte as well.)
 */
public class NibbleCheckitChecksum implements ProofReaderChecksum {
    private int checksum = 0;

    @Override
    public void reset() {
        this.checksum = 0;
    }

    @Override
    public void add(int value) {
        assert value >= 0 && value <= 0xff;
        int work = (checksum << 8) | value;
        for (int i = 0; i < 8; i++) {
            work <<= 1;
            // Note: If we run into negative issues somehow, this could also be "((work & 0xff000000) != 0)".
            if (work > 0xffffff) {
                work &= 0xffffff;
                work ^= 0x102100;
            }
        }
        checksum = work >> 8;
    }

    @Override
    public int value() {
        return checksum;
    }
}
