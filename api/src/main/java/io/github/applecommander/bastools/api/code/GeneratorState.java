package io.github.applecommander.bastools.api.code;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Track current state of the code generation.  This class proxies a number of objects and can be extended 
 * for those objects as required.
 */
public class GeneratorState {
    private final int startAddress;
    private boolean markMoved = false;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public GeneratorState(int startAddress) { 
        this.startAddress = startAddress;
    }
    
    /** Clear current state for another pass.  Used while the generation is "settling down". */
    public void reset() {
        this.markMoved = false;
        this.outputStream.reset();
    }
    
    /** Indicates if a CodeMark has moved. */
    public boolean hasMarkMoved() {
        return this.markMoved;
    }
    /** Hook for the CodeMark to be updated and to capture if a change occurred. */
    public void update(CodeMark mark) {
        markMoved |= mark.update(this);
    }
    
    /** Grab the {@code ByteArrayOutputStream}. Only valid once generation is complete. */
    public ByteArrayOutputStream outputStream() {
        return this.outputStream;
    }

    /** This is the current address as defined by the start address + number of bytes generated. */
    public int currentAddress() {
        return startAddress + outputStream.size();
    }
    
    /** Write a byte to the output stream. */
    public void write(int b) {
        outputStream.write(b);
    }
    /** Write entire byte array to the output stream. */
    public void write(byte[] b) throws IOException {
        outputStream.write(b);
    }
}