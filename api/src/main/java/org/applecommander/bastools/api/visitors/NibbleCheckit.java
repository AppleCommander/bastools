package org.applecommander.bastools.api.visitors;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.Visitor;
import org.applecommander.bastools.api.model.Line;
import org.applecommander.bastools.api.model.Program;
import org.applecommander.bastools.api.model.Statement;
import org.applecommander.bastools.api.model.Token;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class NibbleCheckit implements Visitor {
    private final Configuration config;
    private int totalChecksum;
    private int lineChecksum;

    public NibbleCheckit(Configuration config) {
        this.config = config;
    }

    @Override
    public Program visit(Program program) {
        System.out.println("Nibble Checkit, Copyright 1988, Microsparc Inc.");
        try {
            return Visitor.super.visit(program);
        } finally {
            System.out.printf("TOTAL: %02X%02X\n", totalChecksum & 0xff, totalChecksum >> 8);
        }
    }

    @Override
    public Line visit(Line line) {
        // Calculate and output line value
        lineChecksum = 0;
        for (char ch : Integer.toString(line.lineNumber).toCharArray()) {
            lineChecksum = checkit(lineChecksum, ch|0x80);
        }
        boolean first = true;
        for (Statement statement : line.statements) {
            if (!first) {
                // We need to synthesize the colons
                lineChecksum = checkit(lineChecksum, ':'|0x80);
            }
            Visitor.super.visit(statement);
            first = false;
        }
        int cs = ( (lineChecksum & 0xff) - (lineChecksum >> 8) ) & 0xff;
        System.out.printf("%02X|%s\n", cs, toString(line));

        // Update program values
        totalChecksum = checkit(totalChecksum, line.lineNumber & 0xff);
        totalChecksum = checkit(totalChecksum, line.lineNumber >> 8);
        return line;
    }

    @Override
    public Token visit(Token token) {
        String value = switch (token.type()) {
            case EOL, DIRECTIVE -> "";
            case DATA, SYNTAX, IDENT -> token.text();
            case COMMENT -> "REM";
            case KEYWORD -> token.keyword().text;
            case NUMBER -> {
                if (token.text() != null) {
                    yield token.text();
                }
                yield config.numberToString(token);
            }
            case STRING -> String.format("\"%s\"", token.text());
        };
        for (char ch : value.toCharArray()) {
            lineChecksum = checkit(lineChecksum, ch|0x80);
        }
        return token;
    }

    public String toString(Line line) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.printf("%d ", line.lineNumber);
        boolean first = true;
        for (Statement statement : line.statements) {
            if (first) {
                first = false;
            } else {
                pw.print(":");
            }
            for (Token token : statement.tokens) {
                switch (token.type()) {
                    case EOL:
                        pw.print("<EOL>");
                        break;
                    case COMMENT:
                        pw.printf("REM %s", token.text());
                        break;
                    case DATA, IDENT, SYNTAX:
                        pw.print(token.text());
                        break;
                    case STRING:
                        pw.printf("\"%s\"", token.text());
                        break;
                    case KEYWORD:
                        pw.printf(" %s ", token.keyword().text);
                        break;
                    case DIRECTIVE:
                        pw.printf("%s ", token.text());
                        break;
                    case NUMBER:
                        pw.print(config.numberToString(token));
                        break;
                }
            }
        }
        return sw.toString();
    }

    // TODO: Convert to 16 bits???
    public int checkit(int checksum, int value) {
        assert value >= 0 && value <= 0xff;
        int lo = checksum & 0xff;
        int hi = checksum >> 8;
        for (int i=0; i<8; i++) {
            State state = new State();
            value = state.asl(value);
            lo = state.rol(lo);
            hi = state.rol(hi);
            if (state.carry != 0) {
                lo ^= 0x21;
                hi ^= 0x10;
            }
        }
        return (hi << 8) | lo;
    }

    static class State {
        int carry;
        int asl(int value) {
            carry = (value > 127) ? 1 : 0;
            return (value << 1) & 0xff;
        }
        int rol(int value) {
            int n = carry;
            return asl(value) | n;
        }
    }
}
