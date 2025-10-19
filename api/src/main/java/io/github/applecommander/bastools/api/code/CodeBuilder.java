package io.github.applecommander.bastools.api.code;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * {@code CodeBuilder} allows dynamic generation of combined BASIC and Assembly code with dynamic 
 * {@code CodeMark} capability.  This allows forward references to unknown address in a (mostly) safe
 * manner.
 */
public class CodeBuilder {
    private CodeGenerator generatorChain = (os) -> {};
    
    /** 
     * Generate this set of code beginning at the starting address.
     * @return ByteArrayOutputStream which allows {@code ByteArrayOutputStream#writeTo(java.io.OutputStream)} 
     *         and {@code ByteArrayOutputStream#toByteArray()} 
     */
    public ByteArrayOutputStream generate(int startAddress) throws IOException {
        GeneratorState state = new GeneratorState(startAddress);
        do {
            state.reset();
            generatorChain.generate(state);
        } while (state.hasMarkMoved());
        return state.outputStream();
    }

    /** Start generating BASIC code. */
    public BasicBuilder basic() {
        return new BasicBuilder(this);
    }
    /** Start generating Assembly code. */
    public AsmBuilder asm() {
        return new AsmBuilder(this);
    }

    /** Helper method to chain in a {@code CodeGenerator}. */
    public CodeBuilder add(CodeGenerator generator) {
        generatorChain = generatorChain.andThen(generator);
        return this;
    }
    /** Set a {@code CodeMark}'s value. */
    public CodeBuilder set(CodeMark mark) {
        return add(state -> {
                // A bit twisted, but this allows the GeneratorState and CodeMark to interact without our intervention.
                state.update(mark);
            });
    }
    /** Add a {@code byte[]} to this stream. */
    public CodeBuilder addBinary(byte[] data) {
        return add(state -> state.write(data));
    }
}