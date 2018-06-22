package io.github.applecommander.bastools.api.shapes;

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
	public final boolean vertical;
	public final boolean horizontal;

	private VectorCommand() {
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
	}
	
	public VectorCommand opposite() {
	    int newDirection = this.ordinal() ^ 0b010;
	    return VectorCommand.values()[newDirection];
	}
}