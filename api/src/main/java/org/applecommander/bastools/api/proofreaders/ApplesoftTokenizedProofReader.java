package org.applecommander.bastools.api.proofreaders;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.model.Program;
import org.applecommander.bastools.api.visitors.ByteVisitor;

/**
 * Standard interface for proofreaders that evaluate a tokenized program.
 */
public interface ApplesoftTokenizedProofReader {
    /**
     * Returns the configuration object to use. The configuration object includes the following details:
     * start address, number preservation settings, and file name.
     */
    Configuration getConfiguration();
    /** Convert the Program to a tokenized byte[] and then process with proofreader. */
    default void addProgram(Program program) {
        ByteVisitor byteVisitor = new ByteVisitor(getConfiguration());
        byteVisitor.visit(program);
        addBytes(byteVisitor.getBytes());
    }
    /** Parses the line and adds to the current checksums. */
    void addBytes(byte... tokenizedProgram);
}
