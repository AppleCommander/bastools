package io.github.applecommander.bastools.api;

import java.util.Queue;

import io.github.applecommander.bastools.api.model.ApplesoftKeyword;
import io.github.applecommander.bastools.api.model.Statement;
import org.junit.Assert;
import org.junit.Test;

import io.github.applecommander.bastools.api.model.Program;
import io.github.applecommander.bastools.api.model.Token;
import io.github.applecommander.bastools.api.utils.TokenBuilder;

import javax.xml.crypto.Data;

import static org.junit.Assert.assertEquals;

public class ParserTest {
    @Test
    public void testBlankLines() {
        Queue<Token> tokens = TokenBuilder.builder()
                .eol()  // Blank line before
                .number(10.0).ident("A").syntax('=').number(42.0).eol()
                .eol()  // Blank line after
                .tokens();

        Parser parser = new Parser(tokens);
        Program program = parser.parse();
        Assert.assertNotNull(program);
    }

    @Test
    public void testMinusInData() {
        Queue<Token> tokens = TokenBuilder.builder()
                // 10 DATA -5
                .number(10.0).keyword(ApplesoftKeyword.DATA).keyword(ApplesoftKeyword.sub).number(5.0).eol()
                .tokens();
        Parser parser = new Parser(tokens);
        Program program = parser.parse();
        assertEquals(1, program.lines.size());
        assertEquals(1, program.lines.get(0).statements.size());
        Statement statement = program.lines.get(0).statements.get(0);
        assertEquals(3, statement.tokens.size());
        assertEquals(ApplesoftKeyword.DATA, statement.tokens.get(0).keyword);
        // In this case, the "-" is treated as text
        assertEquals("-", statement.tokens.get(1).text);
        assertEquals(Double.valueOf(5.0), statement.tokens.get(2).number);
    }

    @Test
    public void testMinusInExpression() {
        Queue<Token> tokens = TokenBuilder.builder()
                // 10 A=-5
                .number(10.0).ident("A").keyword(ApplesoftKeyword.eq).keyword(ApplesoftKeyword.sub).number(5.0).eol()
                .tokens();

        Parser parser = new Parser(tokens);
        Program program = parser.parse();
        assertEquals(1, program.lines.size());
        assertEquals(1, program.lines.get(0).statements.size());
        Statement statement = program.lines.get(0).statements.get(0);
        assertEquals(4, statement.tokens.size());
        assertEquals(Token.Type.IDENT, statement.tokens.get(0).type);
        assertEquals(ApplesoftKeyword.eq, statement.tokens.get(1).keyword);
        // In this case, the "-" is an actual keyword
        assertEquals(ApplesoftKeyword.sub, statement.tokens.get(2).keyword);
        assertEquals(Double.valueOf(5.0), statement.tokens.get(3).number);
    }
}
