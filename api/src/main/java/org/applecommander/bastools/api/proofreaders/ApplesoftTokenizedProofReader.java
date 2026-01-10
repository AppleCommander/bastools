/*
 * bastools
 * Copyright (C) 2026  Robert Greene
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
