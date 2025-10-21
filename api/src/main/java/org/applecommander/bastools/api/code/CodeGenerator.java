/*
 * bastools
 * Copyright (C) 2025  Robert Greene
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
package org.applecommander.bastools.api.code;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents a code generation operation that accepts the current {@code GeneratorState}
 * and performs operations against that state.
 */
@FunctionalInterface
public interface CodeGenerator {
    /**
     * Generates code and writes the bytes into the given {@code OutputStream}.
     */
    void generate(GeneratorState state) throws IOException;
    /**
     * Returns a composed {@code CodeGenerator} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Consumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default CodeGenerator andThen(CodeGenerator after) {
        Objects.requireNonNull(after);
        return (GeneratorState state) -> { generate(state); after.generate(state); };
    }
}