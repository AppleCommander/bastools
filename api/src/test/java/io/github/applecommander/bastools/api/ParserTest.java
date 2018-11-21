package io.github.applecommander.bastools.api;

import java.util.Queue;

import org.junit.Assert;
import org.junit.Test;

import io.github.applecommander.bastools.api.model.Program;
import io.github.applecommander.bastools.api.model.Token;
import io.github.applecommander.bastools.api.utils.TokenBuilder;

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
}
