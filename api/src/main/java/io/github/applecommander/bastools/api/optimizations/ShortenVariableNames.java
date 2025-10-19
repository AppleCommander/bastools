package io.github.applecommander.bastools.api.optimizations;

import java.util.Set;

import io.github.applecommander.bastools.api.Configuration;
import io.github.applecommander.bastools.api.Visitors;
import io.github.applecommander.bastools.api.model.Program;
import io.github.applecommander.bastools.api.model.Statement;
import io.github.applecommander.bastools.api.model.Token;
import io.github.applecommander.bastools.api.model.Token.Type;
import io.github.applecommander.bastools.api.utils.VariableNameGenerator;
import io.github.applecommander.bastools.api.visitors.VariableCollectorVisitor;

/**
 * Ensure all variable names are 1 or 2 characters long.
 * This allows the source to use more descriptive variable names, which may
 * crossover ("PLAYERX" and "PLAYERY" become "PL" as far Applesoft BASIC is
 * concerned).  Somewhat hampers running without this optimization being used,
 * however.
 */
public class ShortenVariableNames extends BaseVisitor {
    private final Configuration config;
    private final VariableNameGenerator variableGenerator = new VariableNameGenerator();
    
    public ShortenVariableNames(Configuration config) {
        this.config = config;
    }
    
    @Override
    public Program visit(Program program) {
        // Find existing variable names so we don't clobber already existing names
        VariableCollectorVisitor collector = Visitors.variableCollectorVisitor();
        program.accept(collector);
        Set<String> existingVariables = collector.getVariableNames();
        
        // Preassign all variable names
        for (String originalName : existingVariables) {
            String newName = originalName;
            if (newName.replaceAll("[^\\p{Alnum}]","").length() > 2) {
                String varType = newName.replaceAll("[\\p{Alnum}]","");
                do {
                    newName = variableGenerator.get().orElseThrow(() -> new RuntimeException("Ran out of variable names to assign"));
                    newName += varType;
                } while (existingVariables.contains(newName));
                config.debugStream.printf("Replacing '%s' with '%s'\n", originalName, newName);
            }
            config.variableReplacements.put(originalName, newName);
        }
        // Continue walking the tree to replace the variables!
        return super.visit(program);
    }

    @Override
    public Statement visit(Statement statement) {
        if (statement.tokens.get(0).type == Type.DIRECTIVE) {
            return statement;
        }
        return super.visit(statement);
    }

    @Override
    public Token visit(Token token) {
        if (token.type == Type.IDENT) {
            return Token.ident(token.line, config.variableReplacements.get(token.text));
        }
        return super.visit(token);
    }
}
