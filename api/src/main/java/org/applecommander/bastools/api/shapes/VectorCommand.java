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
package org.applecommander.bastools.api.shapes;

/**
 * Represents all "plot vectors" available in an Applesoft shape table.
 * 
 * @see <a href="https://archive.org/stream/Applesoft_BASIC_Programming_Reference_Manual_Apple_Computer#page/n103/mode/2up">
 *      Applesoft BASIC Programming Reference Manual</a>
 */
public enum VectorCommand {
	// Order here is specific to the encoding within the shape itself
	MOVE_UP, MOVE_RIGHT, MOVE_DOWN, MOVE_LEFT,
	PLOT_UP, PLOT_RIGHT, PLOT_DOWN, PLOT_LEFT;
	
	public final boolean plot;
	public final int xmove;
	public final int ymove;
	
	public final char shortCommand;
	public final String longCommand;
	public final boolean vertical;
	public final boolean horizontal;

	VectorCommand() {
		this.plot = (this.ordinal() & 0b100) != 0;
		// up    0b00
		// right 0b01
		// down  0b10
		// left  0b11
		if ((this.ordinal() & 0b001) == 1) {
			this.xmove = 2 - (this.ordinal() & 0b011);
			this.ymove = 0;
		} else {
			this.xmove = 0;
			this.ymove = (this.ordinal() & 0b011) - 1;
		}
		this.vertical = xmove == 0;
		this.horizontal = ymove == 0;
		
        char shortCommand = "urdl".charAt(this.ordinal() & 0b011);
        this.shortCommand = plot ? Character.toUpperCase(shortCommand) : shortCommand;
        
        this.longCommand = this.name().replaceAll("_", "").toLowerCase();
	}
	
	public VectorCommand opposite() {
	    int newDirection = this.ordinal() ^ 0b010;
	    return VectorCommand.values()[newDirection];
	}
	public VectorCommand plot() {
	    return VectorCommand.values()[this.ordinal() | 0b100];
	}
	public VectorCommand move() {
	    return VectorCommand.values()[this.ordinal() & 0b011];
	}
}