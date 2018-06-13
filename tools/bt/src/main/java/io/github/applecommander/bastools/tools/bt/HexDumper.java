package io.github.applecommander.bastools.tools.bt;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.function.BiConsumer;

/** A slightly-configurable reusable hex dumping mechanism. */
public class HexDumper {
	private PrintStream ps = System.out;
	private int lineWidth = 16;
	private BiConsumer<Integer,Integer> printHeader;
	private BiConsumer<Integer,byte[]> printLine;
	
	public static HexDumper standard() {
		HexDumper hd = new HexDumper();
		hd.printHeader = hd::emptyHeader;
		hd.printLine = hd::standardLine;
		return hd;
	}
	public static HexDumper apple2() {
		HexDumper hd = new HexDumper();
		hd.printHeader = hd::apple2Header;
		hd.printLine = hd::apple2Line;
		return hd;
	}
	
	public void dump(int address, byte[] data) {
		printHeader.accept(address, data.length);
		int offset = 0;
		while (offset < data.length) {
			byte[] line = Arrays.copyOfRange(data, offset, Math.min(offset+lineWidth,data.length));
			printLine.accept(address+offset, line);
			offset += line.length;
		}
	}
	
	public void emptyHeader(int address, int length) {
		// Do Nothing
	}
	public void apple2Header(int address, int length) {
		int end = address + length;
		printLine.accept(0x67, new byte[] { (byte)(address&0xff), (byte)(address>>8), (byte)(end&0xff), (byte)(end>>8) });
		printLine.accept(address-1, new byte[] { 0x00 });
	}
	
	public void standardLine(int address, byte[] data) {
		ps.printf("%04x: ", address);
		for (int i=0; i<lineWidth; i++) {
			if (i < data.length) {
				ps.printf("%02x ", data[i]);
			} else {
				ps.printf(".. ");
			}
		}
		ps.print(" ");
		for (int i=0; i<lineWidth; i++) {
			char ch = ' ';
			if (i < data.length) {
				byte b = data[i];
				ch = (b >= ' ') ? (char)b : '.';
			}
			ps.printf("%c", ch);
		}
		ps.printf("\n");
	}
	public void apple2Line(int address, byte[] data) {
		ps.printf("%04X:", address);
		for (byte b : data) ps.printf("%02X ", b);
		ps.printf("\n");
	}
}
