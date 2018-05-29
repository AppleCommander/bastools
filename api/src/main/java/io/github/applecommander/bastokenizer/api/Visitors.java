package io.github.applecommander.bastokenizer.api;

import java.io.PrintStream;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import io.github.applecommander.bastokenizer.api.visitors.ByteVisitor;
import io.github.applecommander.bastokenizer.api.visitors.LineNumberTargetCollector;
import io.github.applecommander.bastokenizer.api.visitors.PrettyPrintVisitor;
import io.github.applecommander.bastokenizer.api.visitors.PrintVisitor;
import io.github.applecommander.bastokenizer.api.visitors.ReassignmentVisitor;
import io.github.applecommander.bastokenizer.api.visitors.VariableReportVisitor;

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
	
	public static Visitor variableReportVisitor() {
		return new VariableReportVisitor();
	}
}
