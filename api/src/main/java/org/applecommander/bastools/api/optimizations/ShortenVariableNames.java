/*
 * bastools
 * Copyright (C) 2025  Robert Greene
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.applecommander.bastools.api.optimizations;

import java.util.Set;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.Visitors;
import org.applecommander.bastools.api.model.Program;
import org.applecommander.bastools.api.model.Statement;
import org.applecommander.bastools.api.model.Token;
import org.applecommander.bastools.api.model.Token.Type;
import org.applecommander.bastools.api.utils.VariableNameGenerator;
import org.applecommander.bastools.api.visitors.VariableCollectorVisitor;

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
        if (statement.tokens.getFirst().type == Type.DIRECTIVE) {
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
