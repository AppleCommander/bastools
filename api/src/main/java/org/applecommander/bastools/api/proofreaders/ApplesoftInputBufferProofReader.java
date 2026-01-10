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
import org.applecommander.bastools.api.Visitor;
import org.applecommander.bastools.api.Visitors;
import org.applecommander.bastools.api.model.Program;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Standard interface for proofreaders that are evaluated as code is typed in.
 * That is, these evaluate the input buffer at $200 when a return is issued.
 */
public interface ApplesoftInputBufferProofReader {
    /**
     * Returns the configuration object to use. The configuration object includes the following details:
     * start address, number preservation settings, and file name.
     */
    Configuration getConfiguration();
    /** Adds program by converting to text and using addProgramText. */
    default void addProgram(Program program) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        Visitor printVisitor = Visitors.printBuilder(getConfiguration()).printStream(printStream).print().build();
        printVisitor.visit(program);
        addProgramText(outputStream.toString());
    }
    /** Adds source code line-by-line using addLine. */
    default void addProgramText(String code) {
        code.lines().forEach(this::addLine);
    }
    /** Parses the line and adds to the current checksums. */
    void addLine(String line);
}
