package org.applecommander.bastools.api.optimizations;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.model.Token;

public class ShortenNumbers extends BaseVisitor {
    public ShortenNumbers(Configuration config) {
        if (!config.preserveNumbers) {
            System.err.println("Warning: number preservation should be enabled for shorten numbers optimization.");
        }
    }

    @Override
    public Token visit(Token token) {
        if (token.type() == Token.Type.NUMBER) {
            String text = Double.toString(token.number());
            if (Math.rint(token.number()) == token.number()) {
                text = Integer.toString(token.number().intValue());
            } else {
                while (text.startsWith("0") && text.length() > 1) {
                    text = text.substring(1);
                }
            }
            return Token.number(token.line(), token.number(), text);
        }
        return token;
    }
}
