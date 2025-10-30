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
            populateLine(lineNo, tokens, line);
            tokens.add(Token.eol(lineNo));
        }
        return tokens;
    }

    private static void populateLine(final int lineNo, final LinkedList<Token> tokens, final String line) throws IOException {
        PushbackReader reader = new PushbackReader(new StringReader(line));

        StringBuilder sb = new StringBuilder();
        while (true) {
            int ch = reader.read();
            if (Character.isDigit(ch)) {
                sb.append((char)ch);
            }
            else {
                reader.unread(ch);
                break;
            }
        }
        if (sb.isEmpty()) return;
        tokens.add(Token.number(lineNo, Double.valueOf(sb.toString()), sb.toString()));

        Mode mode = Mode.START;
        sb.setLength(0);
        while (true) {
            int ch = reader.read();
            boolean endOfStatement = (ch == ':' && mode.in(Mode.START, Mode.DATA, Mode.TOKEN)) || (ch == -1);
            if (endOfStatement && !sb.isEmpty()) {
                switch (mode) {
                    case START -> tokens.add(Token.syntax(lineNo, sb.charAt(0)));
                    case QUOTE, DATA -> tokens.add(Token.string(lineNo, sb.toString()));
                    case REM -> tokens.add(Token.comment(lineNo, sb.toString()));
                    case TOKEN -> mode = parseToken(lineNo, tokens, sb);
                    case NUMBER -> tokens.add(Token.number(lineNo, Double.valueOf(sb.toString()), sb.toString()));
                }
                sb.setLength(0);
            }
            if (ch == -1) break;

            if (endOfStatement) {
                tokens.add(Token.syntax(lineNo, ch));
                continue;
            }

            switch (mode) {
                case START -> {
                    if (Character.isWhitespace(ch)) continue;
                    sb.append((char)ch);
                    if (ch == '"') {
                        mode = Mode.QUOTE;
                    }
                    else if (Character.isDigit(ch) || ch == '.') {
                        mode = Mode.NUMBER;
                    }
                    else {
                        mode = parseToken(lineNo, tokens, sb);
                    }
                }
                case QUOTE -> {
                    sb.append((char)ch);
                    if (ch == '"') {
                        tokens.add(Token.string(lineNo, sb.toString()));
                        sb.setLength(0);
                        mode = Mode.START;
                    }
                }
                case DATA, REM -> sb.append((char) ch);
                case TOKEN -> {
                    if (Character.isWhitespace(ch)) continue;
                    sb.append((char) ch);
                    mode = parseToken(lineNo, tokens, sb);
                }
                case NUMBER -> {
                    if (Character.isDigit(ch) || ch == '.') {
                        sb.append((char)ch);
                    }
                    else {
                        tokens.add(Token.number(lineNo, Double.valueOf(sb.toString()), sb.toString()));
                        sb.setLength(0);
                        reader.unread(ch);
                        mode = Mode.START;
                    }
                }
            }
        }
    }

    private static Mode parseToken(final int lineNo, final LinkedList<Token> tokens, final StringBuilder sb) {
        Optional<ApplesoftKeyword> found = Arrays.stream(ApplesoftKeyword.values())
                .filter(kw -> {
                    final var kwtxt = kw.text.toUpperCase();
                    final var sbtxt = sb.toString().toUpperCase();
                    return sbtxt.endsWith(kwtxt);
                })
                .findAny();
        Mode mode = Mode.TOKEN;
        if (found.isPresent()) {
            ApplesoftKeyword kw = found.get();
            int n = sb.length() - kw.text.length();
            if (n > 0) {
                tokens.add(Token.ident(lineNo, sb.substring(0, n)));
            }
            tokens.add(Token.keyword(lineNo, kw));
            sb.setLength(0);
            mode = switch (kw) {
                case REM -> Mode.REM;
                case DATA -> Mode.DATA;
                default -> Mode.START;
            };
        }
        return mode;
    }

    private enum Mode {
        START, QUOTE, DATA, REM, NUMBER, TOKEN;

        public boolean in(Mode... modes) {
            return Set.of(modes).contains(this);
        }
    }
}
