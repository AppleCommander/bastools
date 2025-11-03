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
            new LinePopulator(lineNo, tokens).populate(line);
            tokens.add(Token.eol(lineNo));
        }
        return tokens;
    }

    static class LinePopulator {
        private final int lineNo;
        private final LinkedList<Token> tokens;
        private boolean dataFlag = false;
        private boolean quoteFlag = false;


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
                if (ch == ' ' && !dataFlag && !quoteFlag) {
                    i++;
                    continue;
                }

                if (ch == ':') {
                    dataFlag = false;
                    quoteFlag = false;
                    emitSyntax(ch);
                    i++;
                    continue;
                }

                if (ch == '"' && !dataFlag) {
                    // We don't store the quote (because original token reader does not so the tooling synthesizes it for us)
                    quoteFlag = !quoteFlag;
                    i++;
                    continue;
                }

                if (quoteFlag || dataFlag) {
                    emitString(ch);
                    i++;
                    continue;
                }

                if (ch == '?') {
                    emitKeyword(ApplesoftKeyword.PRINT);
                    i++;
                    continue;
                }

                // Keyword handling
                if (Character.isLetter(ch)) {
                    int n = handleKeyword(i, line);
                    if (n == -1) {
                        // No keyword found, must be identifier
                        emitIdent(ch);
                        n = 1;
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

        private void emitSyntax(char ch) {
            tokens.add(Token.syntax(lineNo, ch));
        }
        private void emitString(char ch) {
            String str = extendString(ch, Token.Type.STRING);
            tokens.add(Token.string(lineNo, str));
        }
        private void emitKeyword(ApplesoftKeyword kw) {
            tokens.add(Token.keyword(lineNo, kw));
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
