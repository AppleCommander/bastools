package org.applecommander.bastools.api;

import org.applecommander.bastools.api.model.ApplesoftKeyword;
import org.applecommander.bastools.api.model.Token;
import org.applecommander.bastools.api.model.Tokens;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import static org.junit.Assert.*;

public class ClassicTokenReaderTest {
    @Test
    public void testSimple() throws IOException {
        testCode("10 TEXT:HOME",
            Tokens.builder()
                .number("10")
                    .keyword(ApplesoftKeyword.TEXT)
                    .syntax(':')
                    .keyword(ApplesoftKeyword.HOME)
                .end());
    }

    @Test
    public void preserveNumbers49() throws IOException {
        testCode("""
                10 PRINT "MATHING"
                30 A = .4
                40 B = 0.6000
                50 C = -.250
                60 D = -0.70
                """,
            Tokens.builder()
                .number("10")
                    .keyword(ApplesoftKeyword.PRINT)
                    .string("MATHING")
                    .nextLine()
                .number("30")
                    .ident("A")
                    .keyword(ApplesoftKeyword.eq)
                    .number(".4")
                    .nextLine()
                .number("40")
                    .ident("B")
                    .keyword(ApplesoftKeyword.eq)
                    .number("0.6000")
                    .nextLine()
                .number("50")
                    .ident("C")
                    .keyword(ApplesoftKeyword.eq)
                    .keyword(ApplesoftKeyword.sub)
                    .number(".250")
                    .nextLine()
                .number("60")
                    .ident("D")
                    .keyword(ApplesoftKeyword.eq)
                    .keyword(ApplesoftKeyword.sub)
                    .number("0.70")
                .end());
    }

    @Test
    public void preserveNumbers49b() throws IOException {
        testCode("100 CI = 11 * I1 + .4 / I8 - 16 * .50",
                Tokens.builder()
                    .number("100")
                        .ident("CI")
                        .keyword(ApplesoftKeyword.eq)
                        .number("11")
                        .keyword(ApplesoftKeyword.mul)
                        .ident("I1")
                        .keyword(ApplesoftKeyword.add)
                        .number(".4")
                        .keyword(ApplesoftKeyword.div)
                        .ident("I8")
                        .keyword(ApplesoftKeyword.sub)
                        .number("16")
                        .keyword(ApplesoftKeyword.mul)
                        .number(".50")
                    .end());
    }

    @Test
    public void testHires() throws IOException {
        testCode("""
                1000 hgr : hcolor= 3
                1010 xdraw c1 at 100,10
                1020 hplot 0,0
                """,
                Tokens.builder()
                    .number("1000")
                        .keyword(ApplesoftKeyword.HGR)
                        .syntax(':')
                        .keyword(ApplesoftKeyword.HCOLOR)
                        .number("3")
                        .nextLine()
                    .number("1010")
                        .keyword(ApplesoftKeyword.XDRAW)
                        .ident("C1")
                        .keyword(ApplesoftKeyword.AT)
                        .number("100")
                        .syntax(',')
                        .number("10")
                        .nextLine()
                    .number("1020")
                        .keyword(ApplesoftKeyword.HPLOT)
                        .number("0")
                        .syntax(',')
                        .number("0")
                    .end());
    }

    @Test
    public void testATcombinations() throws IOException {
        testCode("""
                10 A=ATN(0):HLIN 1,2 AT 3:FOR I=A TO N
                """,
                Tokens.builder()
                    .number("10")
                        .ident("A")
                        .keyword(ApplesoftKeyword.eq)
                        .keyword(ApplesoftKeyword.ATN)
                        .syntax('(')
                        .number("0")
                        .syntax(')')
                        .syntax(':')
                        .keyword(ApplesoftKeyword.HLIN)
                        .number("1")
                        .syntax(',')
                        .number("2")
                        .keyword(ApplesoftKeyword.AT)
                        .number("3")
                        .syntax(':')
                        .keyword(ApplesoftKeyword.FOR)
                        .ident("I")
                        .keyword(ApplesoftKeyword.eq)
                        .ident("A")
                        .keyword(ApplesoftKeyword.TO)
                        .ident("N")
                    .end());
    }

    @Test
    public void doesThisActuallyResolveAppleCommanderIssue43() throws IOException {
        testCode("""
                0FORX=1TO100:?X:NEXTX
                """,
                Tokens.builder()
                    .number("0")
                        .keyword(ApplesoftKeyword.FOR)
                        .ident("X")
                        .keyword(ApplesoftKeyword.eq)
                        .number("1")
                        .keyword(ApplesoftKeyword.TO)
                        .number("100")
                        .syntax(':')
                        .keyword(ApplesoftKeyword.PRINT)
                        .ident("X")
                        .syntax(':')
                        .keyword(ApplesoftKeyword.NEXT)
                        .ident("X")
                    .end());
    }

    @Test
    public void testIssue48DATA() throws IOException {
        testCode("70 DATA 0,- 5,3,0,- 100,60,- 40,1,0,1",
            Tokens.builder()
                .number("70")
                    .keyword(ApplesoftKeyword.DATA)
                    // EVERYTHING AFTER DATA IS PRESERVED AS-IS (until ':' or end of line)
                    .data(" 0,- 5,3,0,- 100,60,- 40,1,0,1")
                .end());
    }

    @Test
    public void testREM() throws IOException {
        testCode("""
                    10 TEXT:HOME:REM SOME:COMMENT
                    20 END
                    """,
            Tokens.builder()
                .number("10")
                    .keyword(ApplesoftKeyword.TEXT)
                    .syntax(':')
                    .keyword(ApplesoftKeyword.HOME)
                    .syntax(':')
                    .comment(" SOME:COMMENT")
                    .nextLine()
                .number("20")
                    .keyword(ApplesoftKeyword.END)
                .end());
    }

    @Test
    public void testIssue47a() throws IOException {
        testCode("10 OUT=40:IN=10",
            Tokens.builder()
                .number("10")
                    .ident("OUT")
                    .keyword(ApplesoftKeyword.eq)
                    .number("40")
                    .syntax(':')
                    .ident("IN")
                    .keyword(ApplesoftKeyword.eq)
                    .number("10")
                .end());
    }

    @Test
    public void testIssue47b() throws IOException {
        testCode("""
                20 INQ = 767
                30 IN = IN + 1
                40 CALL IN
                50 PRINT PEEK(IN)
                """,
                Tokens.builder()
                    .number("20")
                        .ident("INQ")
                        .keyword(ApplesoftKeyword.eq)
                        .number("767")
                        .nextLine()
                    .number("30")
                        .ident("IN")
                        .keyword(ApplesoftKeyword.eq)
                        .ident("IN")
                        .keyword(ApplesoftKeyword.add)
                        .number("1")
                        .nextLine()
                    .number("40")
                        .keyword(ApplesoftKeyword.CALL)
                        .ident("IN")
                        .nextLine()
                    .number("50")
                        .keyword(ApplesoftKeyword.PRINT)
                        .keyword(ApplesoftKeyword.PEEK)
                        .syntax('(')
                        .ident("IN")
                        .syntax(')')
                    .end());
    }

    @Test
    public void testApostropheComment() throws IOException {
        testCode("""
                ' Comment the doesn't make it into tokens
                10 PRINT "'SUP?":REM REMARK MAKES IT INTO TOKEN STREAM
                """,
            Tokens.builder()
                .nextLine()     // that comment just creates a newline (per ModernTokenReader)
                .number("10")
                    .keyword(ApplesoftKeyword.PRINT)
                    .string("'SUP?")
                    .syntax(':')
                    .comment(" REMARK MAKES IT INTO TOKEN STREAM")
                .end());
    }

    @Test
    public void testLineContinuation() throws IOException {
        testCode("""
                10 TEXT \\
                 : HOME \\
                 : PRINT "HELLO, WORLD" \\
                 : END
                """,
            Tokens.builder()
                .number("10")
                    .keyword(ApplesoftKeyword.TEXT)
                    .syntax(':')
                    .keyword(ApplesoftKeyword.HOME)
                    .syntax(':')
                    .keyword(ApplesoftKeyword.PRINT)
                    .string("HELLO, WORLD")
                    .syntax(':')
                    .keyword(ApplesoftKeyword.END)
                .end());
    }

    @Test
    public void testStringArrayIssue49() throws IOException {
        testCode("NAME$(2)",
            Tokens.builder()
                .ident("NAME$")
                .syntax('(')
                .number("2")
                .syntax(')')
                .end());
    }

    @Test
    public void testZeroLengthString() throws IOException {
        testCode("A$=\"\"",
            Tokens.builder()
                .ident("A$")
                .keyword(ApplesoftKeyword.eq)
                .string("")
                .end());
    }

    @Test
    public void testPrintTwoStrings() throws IOException {
        testCode("PRINT \"HELLO\"\"WORLD\"",
            Tokens.builder()
                .keyword(ApplesoftKeyword.PRINT)
                .string("HELLO")
                .string("WORLD")
                .end());
    }

    @Test
    public void testStringFiasco49() throws IOException {
        testCode("PRINT \": \";G$",
            Tokens.builder()
                .keyword(ApplesoftKeyword.PRINT)
                .string(": ")
                .syntax(';')
                .ident("G$")
                .end());
    }

    @Test
    public void testColonInDATA() throws IOException {
        testCode("DATA \"Name: \"",
            Tokens.builder()
                .keyword(ApplesoftKeyword.DATA)
                .data(" \"Name: \"")
                .end());
    }

    @Test
    public void testKeywordInDATA() throws IOException {
        testCode("10 DATA TEXT",
            Tokens.builder()
                .number("10")
                    .keyword(ApplesoftKeyword.DATA)
                    .data(" TEXT")
                .end());
    }

    public void testCode(String code, Token... expectedTokens) throws IOException {
        final Queue<Token> actualTokens = ClassicTokenReader.tokenize(new StringReader(code));
        final Token[] actualArray = actualTokens.toArray(new Token[0]);
        int max = Math.max(expectedTokens.length, actualTokens.size());
        List<String> errors = new ArrayList<>();
        for (int i=0; i<max; i++) {
            Token expected = null;
            if (i < expectedTokens.length) {
                expected = expectedTokens[i];
            } else {
                errors.add("Expected token list is shorter than actual");
                break;
            }
            Token actual = null;
            if (i < actualArray.length) {
                actual = actualArray[i];
            } else {
                errors.add("Actual token list is shorter than expected");
                break;
            }
            if (!compareToken(expected, actual)) {
                String msg = String.format("Token %d mismatch: '%s' != '%s'", i, expected, actual);
                errors.add(msg);
            }
        }
        if (!errors.isEmpty()) {
            fail(String.join("\n", errors));
        }
    }

    /**
     * Selectively compare token details. Specifically, skip the line number
     * since that doesn't always aling and isn't pertinent to these tests.
     */
    public boolean compareToken(Token expected, Token actual) {
        return expected.type() == actual.type()
            && Objects.equals(expected.text(), actual.text())
            && expected.keyword() == actual.keyword()
            && Objects.equals(expected.number(), actual.number());
    }
}
