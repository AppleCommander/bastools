package io.github.applecommander.bastools.api.code;

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