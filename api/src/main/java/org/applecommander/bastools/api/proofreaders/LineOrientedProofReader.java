package org.applecommander.bastools.api.proofreaders;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.Visitor;
import org.applecommander.bastools.api.model.Line;
import org.applecommander.bastools.api.model.Statement;
import org.applecommander.bastools.api.model.Token;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class LineOrientedProofReader implements Visitor {
    protected final Configuration config;

    protected LineOrientedProofReader(Configuration config) {
        this.config = config;
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
}
