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
package org.applecommander.bastools.tools.bt;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.function.BiConsumer;

/** A slightly-configurable reusable hex dumping mechanism. */
public class HexDumper {
	private final PrintStream ps = System.out;
	private final int lineWidth = 16;
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
		int end = address + length + 1;
		printLine.accept(0x67, new byte[] {
                (byte)(address&0xff), (byte)(address>>8)        // $67-68: Start of Applesoft program
        });
        printLine.accept(0xaf, new byte[] {
                (byte)(end&0xff), (byte)(end>>8),               // $AF-B0: End of Applesoft program
        });
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
