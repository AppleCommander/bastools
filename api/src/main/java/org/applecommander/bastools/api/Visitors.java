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
package org.applecommander.bastools.api;

import java.io.PrintStream;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.applecommander.bastools.api.visitors.ByteVisitor;
import org.applecommander.bastools.api.visitors.LineNumberTargetCollector;
import org.applecommander.bastools.api.visitors.PrettyPrintVisitor;
import org.applecommander.bastools.api.visitors.PrintVisitor;
import org.applecommander.bastools.api.visitors.ReassignmentVisitor;
import org.applecommander.bastools.api.visitors.VariableCollectorVisitor;
import org.applecommander.bastools.api.visitors.VariableReportVisitor;

/**
 * This class presents all of the common Visitor implementations via builder patterns.
 * The number is currently small enough that all the builders and visitors are defined 
 * in this one class.
 *  
 * @author rob
 */
public class Visitors {
	public static PrintBuilder printBuilder() {
		return new PrintBuilder();
	}
	public static class PrintBuilder {
		private PrintStream printStream = System.out;
		private Function<PrintBuilder,Visitor> creator = PrintVisitor::new;
		
		public PrintBuilder printStream(PrintStream printStream) {
			Objects.requireNonNull(printStream);
			this.printStream = printStream;
			return this;
		}
		public PrintBuilder prettyPrint(boolean flag) {
			creator = flag ? PrettyPrintVisitor::new : PrintVisitor::new;
			return this;
		}
		public PrintBuilder prettyPrint() {
			creator = PrettyPrintVisitor::new;
			return this;
		}
		public PrintBuilder print() {
			creator = PrintVisitor::new;
			return this;
		}
		
		public Visitor build() {
			return creator.apply(this);
		}
		public PrintStream getPrintStream() {
			return printStream;
		}
	}
	
	public static ByteVisitor byteVisitor(Configuration config) {
		return new ByteVisitor(config);
	}
	
	/** Rewrite the Program tree with the line number reassignments given. */
	public static ReassignmentVisitor reassignVisitor(Map<Integer,Integer> reassignments) {
		return new ReassignmentVisitor(reassignments);
	}
	
	/** Collect all line numbers that are a target of GOTO, GOSUB, etc. */
	public static LineNumberTargetCollector lineNumberTargetCollector() {
		return new LineNumberTargetCollector();
	}
	
	public static VariableCollectorVisitor variableCollectorVisitor() {
		return new VariableCollectorVisitor();
	}
	
	public static Visitor variableReportVisitor() {
		return new VariableReportVisitor();
	}
}
