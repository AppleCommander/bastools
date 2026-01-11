package org.applecommander.bastools.api.proofreaders;

/**
 * Standard interface for proofreaders that evaluate a binary program.
 */
public interface BinaryProofReader {
    default void addProgram(String text) {
        // TODO
    }
    /** Parses the line and adds to the current checksums. */
    void addBytes(int address, byte... binary);
}
