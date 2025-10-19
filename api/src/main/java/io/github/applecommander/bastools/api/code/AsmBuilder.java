package io.github.applecommander.bastools.api.code;

import java.io.IOException;
import java.util.Objects;

/**
 * {@code AsmBuilder} allows generation of assembly code to embed into the output stream.
 * <p>
 * By no means is this complete, but is being built out as the need arises.
 */
public class AsmBuilder {
    private final CodeBuilder builder;
    
    public AsmBuilder(CodeBuilder builder) {
        Objects.requireNonNull(builder);
        this.builder = builder;
    }
    public CodeBuilder end() {
        return this.builder;
    }
    
    /** Generate a "LDY #value" in the output stream. */
    public AsmBuilder ldy(int value) {
        builder.add(state -> internalLDY(state, value));
        return this;
    }
    /** Generate a "JMP address" in the output stream. */
    public AsmBuilder jmp(int address) {
        builder.add(state -> internalJMP(state, address));
        return this;
    }
    /** Generate a "LDA #value" in the output stream. */
    public AsmBuilder lda(int value) {
        builder.add(state -> internalLDA(state, value));
        return this;
    }
    /** Generate a "STA address" in the output stream. */
    public AsmBuilder sta(int address) {
        builder.add(state -> internalSTA(state, address));
        return this;
    }
    /**
     * Generate an address setup in the output stream of the format:
     * <pre>
     * LDA #low(value)
     * STA address
     * LDA #high(value)
     * STA address+1
     * </pre> 
     */
    public AsmBuilder setAddress(int value, int address) {
        builder.add(state -> {
            internalLDA(state, value & 0xff);
            internalSTA(state, address);
            internalLDA(state, value >> 8);
            internalSTA(state, address+1);
        });
        return this;
    }
    /**
     * Generate an address setup for a mark in the output stream of the format:
     * <pre>
     * LDA #low(mark)
     * STA address
     * LDA #high(mark)
     * STA address+1
     * </pre> 
     */
    public AsmBuilder setAddress(CodeMark mark, int address) {
        builder.add(state -> {
            int value = mark.getAddress();
            internalLDA(state, value & 0xff);
            internalSTA(state, address);
            internalLDA(state, value >> 8);
            internalSTA(state, address+1);
        });
        return this;
    }
 
    private void internalJMP(GeneratorState state, int address) {
        state.write(0x4c);
        state.write(address & 0xff);
        state.write(address >> 8);
    }
    private void internalLDY(GeneratorState state, int value) {
        state.write(0xa0);
        state.write(value);
    }
    private void internalLDA(GeneratorState state, int value) {
        state.write(0xa9);
        state.write(value);
    }
    private void internalSTA(GeneratorState state, int address) {
        if ((address & 0xff00) == 0) {
            state.write(0x85);
            state.write(address);
        } else {
            throw new RuntimeException("sta does not handle 16 bit addresses yet!");
        }
    }
}