package io.github.applecommander.bastools.api.code;

import java.util.Objects;

import io.github.applecommander.bastools.api.model.ApplesoftKeyword;

/**
 * {@code BasicBuilder} allows BASIC commands to be built.  Note that {@code #endLine()} and {{@link #endStatement()}
 * are items that need to be invoked by hand.
 * <p>
 * By no means is this complete, but is being built out as the need arises.
 */
public class BasicBuilder {
    private CodeBuilder builder;
    public BasicBuilder(CodeBuilder builder) {
        Objects.requireNonNull(builder);
        this.builder = builder;
    }
    public CodeBuilder end() {
        return this.builder;
    }
    /** Generate a "RETURN" statement. */
    public BasicBuilder RETURN() {
        builder.add(state -> state.write(ApplesoftKeyword.RETURN.code));
        return this;
    }
    /** Generate a "GOTO <lineNumber>" statement. */
    public BasicBuilder GOTO(int lineNumber) {
        builder.add(state -> {
            state.write(ApplesoftKeyword.GOTO.code);
            state.write(Integer.toString(lineNumber).getBytes());
        });
        return this;
    }
    /** Generate a "GOSUB <lineNumber>" statement. */
    public BasicBuilder GOSUB(int lineNumber) {
        builder.add(state -> {
            state.write(ApplesoftKeyword.GOSUB.code);
            state.write(Integer.toString(lineNumber).getBytes());
        });
        return this;
    }
    /** Generate a "CALL <markAddress>" statement. */
    public BasicBuilder CALL(CodeMark mark) {
        builder.add(state -> {
            int address = mark.getAddress();
            state.write(ApplesoftKeyword.CALL.code);
            state.write(Integer.toString(address).getBytes());
        });
        return this;
    }
    /** Generate a "POKE <address>,<lowMarkAddress>:POKE <address+1>,<highMarkAddress>" set of statements. */
    public BasicBuilder POKEW(int address, CodeMark mark) {
        builder.add(state -> {
            int value = mark.getAddress();
            state.write(ApplesoftKeyword.POKE.code);
            state.write(Integer.toString(address).getBytes());
            state.write(',');
            state.write(Integer.toString(value & 0xff).getBytes());
            state.write(':');
            state.write(ApplesoftKeyword.POKE.code);
            state.write(Integer.toString(address+1).getBytes());
            state.write(',');
            state.write(Integer.toString(value >> 8).getBytes());
        });
        return this;
    }
    /** Generate a statement separator. */
    public BasicBuilder endStatement() {
        builder.add(state -> state.write(':'));
        return this;
    }
    /** Generate an assignment statement. */
    public BasicBuilder assign(String varName, CodeMark mark) {
        builder.add(state -> {
            state.write(varName.getBytes());
            state.write(ApplesoftKeyword.eq.code);
            state.write(Integer.toString(mark.getAddress()).getBytes());
        });
        return this;
    }
    /** Generate an assignment statement. */
    public BasicBuilder assign(String varName, int value) {
        builder.add(state -> {
            state.write(varName.getBytes());
            state.write(ApplesoftKeyword.eq.code);
            state.write(Integer.toString(value).getBytes());
        });
        return this;
    }
    /** End the current line. No more BASIC after this point! */
    public CodeBuilder endLine() {
        builder.add(state -> state.write(0x00));
        return builder;
    }
    /** Generate a "ROT=<0-64>" statement. */
    public BasicBuilder ROT(int lineNumber) {
        builder.add(state -> {
            state.write(ApplesoftKeyword.ROT.code);
            state.write(Integer.toString(lineNumber).getBytes());
        });
        return this;
    }
    /** Generate a "SCALE=<1-255>" statement. */
    public BasicBuilder SCALE(int lineNumber) {
        builder.add(state -> {
            state.write(ApplesoftKeyword.SCALE.code);
            state.write(Integer.toString(lineNumber).getBytes());
        });
        return this;
    }
    /** Generate a "HCOLOR=<0-7>" statement. */
    public BasicBuilder HCOLOR(int lineNumber) {
        builder.add(state -> {
            state.write(ApplesoftKeyword.HCOLOR.code);
            state.write(Integer.toString(lineNumber).getBytes());
        });
        return this;
    }
}
