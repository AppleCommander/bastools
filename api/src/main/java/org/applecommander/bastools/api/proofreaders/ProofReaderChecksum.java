package org.applecommander.bastools.api.proofreaders;

public interface ProofReaderChecksum {
    /** Reset the checksum back to initial state. */
    void reset();
    /** Add a value (typically a byte) into the checksum. */
    void add(int value);
    /** Get the current value of the checksum. */
    int value();
}
