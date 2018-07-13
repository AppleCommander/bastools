package io.github.applecommander.bastools.api.code;

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
        try {
            return currentAddress != address;
        } finally {
            this.address = currentAddress;
        }
    }
}
