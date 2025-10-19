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
package io.github.applecommander.bastools.tools.st;

import java.util.Optional;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;

/**
 * Primary entry point into the Shape Tools utility. 
 */
@Command(name = "st", mixinStandardHelpOptions = true, versionProvider = VersionProvider.class,
    descriptionHeading = "%n",
    commandListHeading = "%nCommands:%n",
    optionListHeading = "%nOptions:%n",
    description = "Shape Tools utility", 
    subcommands = {
            ExtractCommand.class,
            GenerateCommand.class,
            HelpCommand.class, 
            })
public class Main implements Runnable {
    @Option(names = "--debug", description = "Dump full stack traces if an error occurs")
    private static boolean debugFlag;
    
    public static void main(String[] args) {
        try {
            int exitCode = new CommandLine(new Main()).execute(args);
            System.exit(exitCode);
        } catch (Throwable t) {
            if (Main.debugFlag) {
                t.printStackTrace(System.err);
            } else {
                String message = t.getMessage();
                while (t != null) {
                    message = t.getMessage();
                    t = t.getCause();
                }
                System.err.printf("Error: %s\n", Optional.ofNullable(message).orElse("An error occurred."));
            }
            System.exit(1);
        }
    }
    
    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
