package io.github.applecommander.bastools.api.code;

import java.util.HashMap;

/**
 * A {@code CodeMark} marks a dynamic address within the output stream. When referenced, it will report the
 * most recent address is knows, forcing the generation to run multiple times until it "settles".  
 * <p>
 * Multiple passes occur for the following reasons:<ul>
 * <li>an assembly address can be calculated on the second pass (1st is to actually calculate address and 2nd pass is to 
 *     use that address).</li>
 * <li>Applesoft BASIC encodes the address as text; that means first pass, the address is "0" but 2nd pass the address is
 *     likely to be 4 digits "8123" (for example) which, in turn moves anything after that point, requiring a 3rd pass
 *     to push everything out (and likely making that number become "8126" since going from 1 digit to 4 digits adds
 *     3 bytes to the preceding bytes</li>
 * </ul>
 *  
 * @author rob
 */
public class CodeMark {
    private static final int LOOP_MAX = 10;
    private HashMap<Integer,Integer> loopCounter = new HashMap<>();
    private int address;
    
    public int getAddress() {
        return address;
    }
    
    /** 
     * Update the current address based on the {@code GeneratorState}. 
     * @return boolean indicating if the address changed 
     */
    public boolean update(GeneratorState state) {
        int currentAddress = state.currentAddress();
        loopCounter.merge(currentAddress, 1, (a,b) -> a+b);
        if (loopCounter.get(currentAddress) > LOOP_MAX) {
            StringBuilder sb = new StringBuilder();
            sb.append("A circular pattern in a dynamic address was discovered!\n");
            sb.append("This usually indicates that an address was computed to be just below a page boundary.\n");
            sb.append("For example, the 0x1000 mark.  However, code using that address then pushed the\n");
            sb.append("address over the 0x1000 mark, but that triggered the address to be recomputed below\n");
            sb.append("the 0x1000 mark.  Rinse and repeat.\n");
            sb.append("\n");
            sb.append("For example, shape tables have a POKE 232,255 when the shape table is at 0xFFF, but the\n");
            sb.append("address gets recomputed (due to the 3 digit low address) to be 0x1001, which changes the\n");
            sb.append("low address byte to be a single digit. This starts the cascade.\n");
            sb.append("\n");
            sb.append("Generally, stick a little bit of extra code into the program bypasses this issue.\n");
            sb.append("(Sorry, there is no elegant solution at this time.  Pull requests are welcome!).\n");
            throw new IllegalStateException(sb.toString());
        }
        try {
            return currentAddress != address;
        } finally {
            this.address = currentAddress;
        }
    }
}
