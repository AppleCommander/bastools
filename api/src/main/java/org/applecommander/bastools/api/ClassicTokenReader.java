package org.applecommander.bastools.api;

import org.applecommander.bastools.api.model.ApplesoftKeyword;
import org.applecommander.bastools.api.model.Token;

import java.io.*;
import java.util.*;

public class ClassicTokenReader {
    /** A handy method to generate a list of Tokens from a file name. */
    public static Queue<Token> tokenize(String filename) throws IOException {
        try (FileReader fileReader = new FileReader(filename)) {
            return tokenize(fileReader);
        }
    }
    /** A handy method to generate a list of Tokens from a file. */
    public static Queue<Token> tokenize(File file) throws IOException {
        try (FileReader fileReader = new FileReader(file)) {
            return tokenize(fileReader);
        }
    }
    /** A handy method to generate a list of Tokens from an InputStream. */
    public static Queue<Token> tokenize(InputStream inputStream) throws IOException {
        try (InputStreamReader streamReader = new InputStreamReader(inputStream)) {
            return tokenize(streamReader);
        }
    }

    public static Queue<Token> tokenize(Reader reader) throws IOException {
        LineNumberReader lineNumberReader = new LineNumberReader(reader);
        LinkedList<Token> tokens = new LinkedList<>();
        while (true) {
            String line = lineNumberReader.readLine();
            int lineNo = lineNumberReader.getLineNumber();
            if (line == null) break;

            LinePopulator lp = new LinePopulator(lineNo, tokens);
            if (line.endsWith("\\")) {
                // Line continuation -- we just skip the eol bit
                lp.populate(line.substring(0, line.length() - 2));
            }
            else {
                lp.populate(line);
                tokens.add(Token.eol(lineNo));
            }
        }
        return tokens;
    }

    static class LinePopulator {
        /** These are the alternate tokens that do not start with an alphabetic character. */
        private static final Set<Character> ALT_TOKENS = Set.of('&','+','-','*','/','^','<','=','>');
        private final int lineNo;
        private final LinkedList<Token> tokens;
        private boolean dataFlag = false;
        private boolean quoteFlag = false;
        private boolean remFlag = false;

        private LinePopulator(int lineNo, LinkedList<Token> tokens) {
            this.lineNo = lineNo;
            this.tokens = tokens;
        }

        // Inspired by: https://github.com/KrisKennaway/bastoken/blob/master/bastoken.py
        // and referenced with: https://6502disassembly.com/a2-rom/Applesoft.html#SymPARSE
        public void populate(final String line) {
            int i = 0;
            while (i < line.length()) {
                char ch = line.charAt(i);
                if (ch == ' ' && !dataFlag && !quoteFlag && !remFlag) {
                    i++;
                    continue;
                }

                if (ch == ':' && !remFlag) {
                    dataFlag = false;
                    quoteFlag = false;
                    emitSyntax(ch);
                    i++;
                    continue;
                }

                if (ch == '"' && !dataFlag && !remFlag) {
                    // We don't store the quote (because original token reader does not so the tooling synthesizes it for us)
                    quoteFlag = !quoteFlag;
                    i++;
                    continue;
                }

                if (quoteFlag) {
                    emitString(ch);
                    i++;
                    continue;
                }

                if (dataFlag) {
                    emitData(ch);
                    i++;
                    continue;
                }

                if (remFlag) {
                    emitComment(ch);
                    i++;
                    continue;
                }

                if (ch == '?') {
                    emitKeyword(ApplesoftKeyword.PRINT);
                    i++;
                    continue;
                }

                if (ch == '\'') {
                    // This is a non-tokenized (custom) comment to the end of the line
                    break;
                }

                // Keyword handling
                // Additional: &+-*/^<=>
                if (Character.isLetter(ch) || ALT_TOKENS.contains(ch)) {
                    int n = handleKeyword(i, line);
                    if (n == -1) {
                        // No keyword found, must be identifier
                        emitIdent(ch);
                        n = 1;
                    }
                    else {
                        // Need to set some flags. Note we assume it's a keyword due to the return code
                        dataFlag = tokens.getLast().keyword() == ApplesoftKeyword.DATA;
                        remFlag = tokens.getLast().keyword() == ApplesoftKeyword.REM;
                    }
                    i += n;
                    continue;
                }

                // Special handling of digits - we might be handing a variable like "A9", so we detect that...
                if (Character.isDigit(ch)) {
                    if (!tokens.isEmpty() && tokens.getLast().type() == Token.Type.IDENT) {
                        emitIdent(ch);
                    }
                    else {
                        emitNumber(ch);
                    }
                }
                // A "." at this point is expected to be a number
                else if (ch == '.') {
                    emitNumber(ch);
                }
                // A "$" _might_ be a directive, figure that out
                else if (ch == '$') {
                    int n = handleDirective(i, line);
                    if (n == -1) {
                        // No directive, emit as general syntax character
                        emitSyntax(ch);
                        n = 1;
                    }
                    i += n;
                }
                // Else assume we've got general syntax character
                else {
                    emitSyntax(ch);
                }
                i++;
            }
        }

        public int handleKeyword(final int base, final String line) {
            for (ApplesoftKeyword kw : ApplesoftKeyword.values()) {
                int lookahead_idx = base;
                int token_idx = 0;
                while (lookahead_idx < line.length() && token_idx < kw.text.length()) {
                    char ch = line.charAt(lookahead_idx);
                    if (ch == ' ') {
                        lookahead_idx++;
                        continue;
                    }
                    if (Character.toUpperCase(ch) != kw.text.charAt(token_idx)) {
                        break;
                    }
                    if (token_idx == kw.text.length() - 1) {
                        // Figure out AT/ATN/A TO
                        if (kw == ApplesoftKeyword.AT && lookahead_idx+1 < line.length()) {
                            char nextCh = Character.toUpperCase(line.charAt(lookahead_idx+1));
                            if (nextCh == 'N') {
                                lookahead_idx++;
                                kw = ApplesoftKeyword.ATN;
                            }
                            else if (nextCh == 'O') {
                                emitIdent('A');
                                lookahead_idx++;
                                kw = ApplesoftKeyword.TO;
                            }
                        }
                        emitKeyword(kw);
                        return lookahead_idx - base + 1;
                    }
                    lookahead_idx++;
                    token_idx++;
                }
            }
            return -1;
        }

        /** Look ahead for the directive. Note that spaces are important. */
        public int handleDirective(final int base, final String line) {
            for (String directive : Directives.names()) {
                int lookahead_idx = base;
                int directive_idx = 0;
                while (lookahead_idx < line.length() && directive_idx < directive.length()) {
                    char ch = line.charAt(lookahead_idx);
                    if (Character.toUpperCase(ch) != Character.toUpperCase(directive.charAt(directive_idx))) {
                        break;
                    }
                    if (directive_idx == directive.length() - 1) {
                        emitDirective(line.substring(base, lookahead_idx+1));
                        return lookahead_idx - base + 1;
                    }
                    lookahead_idx++;
                    directive_idx++;
                }
            }
            return -1;
        }

        private void emitSyntax(char ch) {
            tokens.add(Token.syntax(lineNo, ch));
        }
        private void emitString(char ch) {
            String str = extendString(ch, Token.Type.STRING);
            tokens.add(Token.string(lineNo, str));
        }
        private void emitData(char ch) {
            String data = extendString(ch, Token.Type.DATA);
            tokens.add(Token.data(lineNo, data));
        }
        private void emitComment(char ch) {
            // Special: COMMENT essentially includes the REM (from "modern" parser) so we need to remove it
            if (!tokens.isEmpty() && tokens.getLast().type() == Token.Type.KEYWORD
                    && tokens.getLast().keyword() == ApplesoftKeyword.REM) {
                tokens.removeLast();
            }
            String comment = extendString(ch, Token.Type.COMMENT);
            tokens.add(Token.comment(lineNo, comment));
        }
        private void emitKeyword(ApplesoftKeyword kw) {
            tokens.add(Token.keyword(lineNo, kw));
        }
        private void emitDirective(String directive) {
            tokens.add(Token.directive(lineNo, directive));
        }
        private void emitNumber(char ch) {
            String num = extendString(ch, Token.Type.NUMBER);
            double value = 0.0;
            if (!".".equals(num)) {
                value = Double.parseDouble(num);
            }
            tokens.add(Token.number(lineNo, value, num));
        }
        private void emitIdent(char ch) {
            String name = extendString(ch, Token.Type.IDENT);
            tokens.add(Token.ident(lineNo, name));
        }
        private String extendString(final char ch, final Token.Type ttype) {
            String str = Character.toString(ch);
            if (!tokens.isEmpty() && tokens.getLast().type() == ttype) {
                str = tokens.getLast().text() + ch;
                tokens.removeLast();
            }
            return str;
        }
    }
}
