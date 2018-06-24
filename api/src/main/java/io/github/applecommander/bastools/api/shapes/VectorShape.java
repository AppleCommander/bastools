package io.github.applecommander.bastools.api.shapes;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VectorShape implements Shape {
    public static VectorShape from(ByteBuffer buf) {
        Objects.requireNonNull(buf);
        VectorShape shape = new VectorShape();

        VectorCommand[] commands = VectorCommand.values();
        while (buf.hasRemaining()) {
            int code = Byte.toUnsignedInt(buf.get());
            if (code == 0) break;
            
            int vector1 = code & 0b111;
            int vector2 = (code >> 3) & 0b111;
            int vector3 = (code >> 6) & 0b011;  // Cannot plot
            
            shape.vectors.add(commands[vector1]);

            if (vector2 != 0 || vector3 != 0) {
                shape.vectors.add(commands[vector2]);
                
                if (vector3 != 0) {
                    shape.vectors.add(commands[vector3]);
                }
            }
        }
        return shape;
    }
    
	public final List<VectorCommand> vectors = new ArrayList<>();
	
	public VectorShape moveUp()    { return append(VectorCommand.MOVE_UP);    } 
	public VectorShape moveRight() { return append(VectorCommand.MOVE_RIGHT); } 
	public VectorShape moveDown()  { return append(VectorCommand.MOVE_DOWN);  } 
	public VectorShape moveLeft()  { return append(VectorCommand.MOVE_LEFT);  } 
	public VectorShape plotUp()    { return append(VectorCommand.PLOT_UP);    } 
	public VectorShape plotRight() { return append(VectorCommand.PLOT_RIGHT); } 
	public VectorShape plotDown()  { return append(VectorCommand.PLOT_DOWN);  } 
	public VectorShape plotLeft()  { return append(VectorCommand.PLOT_LEFT);  }
	
	public VectorShape append(VectorCommand vectorCommand) {
		this.vectors.add(vectorCommand);
		return this;
	}
	
	/** 
	 * Optimize the vectors by removing useless vectors or replacing a series with a shorter series.
	 * At this point, everything is based off of a regex with a potential modification. 
	 */
	public VectorShape optimize() {
	    String commands = toShortCommands();
	    Function<String,String> opts =
	            // Unused moves (left followed by a right with no plotting in between, for instance).
	            VectorRegexOptimization.of("l([ud]*)r")
	            .andThen(VectorRegexOptimization.of("r([ud]*)l"))
                .andThen(VectorRegexOptimization.of("u([rl]*)d"))
                .andThen(VectorRegexOptimization.of("d([rl]*)u"))
                // These are plot/move combinations, such as LEFT>up>right that can be replaced by just UP.
                .andThen(VectorRegexOptimization.of("L([ud])r", String::toUpperCase))
                .andThen(VectorRegexOptimization.of("R([ud])l", String::toUpperCase))
                .andThen(VectorRegexOptimization.of("U([rl])d", String::toUpperCase))
                .andThen(VectorRegexOptimization.of("D([rl])u", String::toUpperCase))
                // Base assumption is that any tail moves can be removed as they don't lead to a plot.
                .andThen(VectorRegexOptimization.of("()[udlr]+$"));
	    
	    String oldCommands = null;
	    do {
	        oldCommands = commands;
	        commands = opts.apply(commands);
	    } while (!oldCommands.equals(commands));
	    
	    VectorShape newShape = new VectorShape();
	    newShape.appendShortCommands(commands);
	    return newShape;
	}
	
	/** 
	 * A vector optimization based on regex.  Transformation is optional to (for instance) change a {@code move} 
	 * to a {@code plot} command.  Note that the regex requires a matcher group; also be aware that an empty group
	 * "{@code ()}" is a viable solution. 
	 */
	public static class VectorRegexOptimization implements Function<String,String> {
	    public static Function<String,String> of(String regex, Function<String,String> transformation) {
	        VectorRegexOptimization opt = new VectorRegexOptimization();
	        opt.pattern = Pattern.compile(regex);
	        opt.fn = transformation;
	        return opt;
	    }
	    public static Function<String,String> of(String regex) {
	        return of(regex, (s) -> s);
	    }
	    
        private Pattern pattern;
        private Function<String,String> fn;
        
	    private VectorRegexOptimization() { /* Prevent construction */ }
	    
	    @Override
	    public String apply(String shortCommands) {
            Matcher matcher = pattern.matcher(shortCommands);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, fn.apply(matcher.group(1)));
            }
            matcher.appendTail(sb);
            return sb.toString();
	    }
	}
	
	public String toShortCommands() {
	    StringBuilder sb = new StringBuilder();
	    vectors.stream().map(v -> v.shortCommand).forEach(sb::append);
	    return sb.toString();
	}
	
	public void appendShortCommands(String line) {
	    for (char cmd : line.trim().toCharArray()) {
	        appendShortCommand(cmd);
	    }
	}
	public void appendShortCommand(char cmd) {
        switch (cmd) {
        case 'u': moveUp();    break;
        case 'd': moveDown();  break;
        case 'l': moveLeft();  break;
        case 'r': moveRight(); break;
        case 'U': plotUp();    break;
        case 'D': plotDown();  break;
        case 'L': plotLeft();  break;
        case 'R': plotRight(); break;
        default:
            // whitespace is allowed
            if (!Character.isWhitespace(cmd)) {
                throw new RuntimeException("Unknown command: " + cmd);
            }
        }
	}
	
	public void appendLongCommands(String line) {
	    Queue<String> tokens = new LinkedList<>(Arrays.asList(line.split("\\s+")));
	    while (!tokens.isEmpty()) {
	        String command = tokens.remove();
	        int count = 1;
	        String checkNumber = tokens.peek();
	        if (checkNumber != null && checkNumber.matches("\\d+")) count = Integer.parseInt(tokens.remove());
	        
	        for (int i=0; i<count; i++) {
	            switch (command.toLowerCase()) {
	            case "moveup":    moveUp();    break;
	            case "movedown":  moveDown();  break;
	            case "moveleft":  moveLeft();  break;
	            case "moveright": moveRight(); break;
	            case "plotup":    plotUp();    break;
	            case "plotdown":  plotDown();  break;
	            case "plotleft":  plotLeft();  break;
	            case "plotright": plotRight(); break;
	            default:
	                throw new RuntimeException("Unknown command: " + command);
	            }
	        }
	    }
	}
	
	public byte[] toBytes() {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    LinkedList<VectorCommand> work = new LinkedList<>(vectors);
	    while (!work.isEmpty()) {
	        VectorCommand vector1 = work.remove();
            int section1 = vector1.ordinal();
	        VectorCommand vector2 = work.poll();
            int section2 = Optional.ofNullable(vector2).map(VectorCommand::ordinal).orElse(0);
            VectorCommand vector3 = work.poll();
            if (vector3 != null && vector3.plot) {
                work.addFirst(vector3);
                vector3 = null;
            }
            int section3 = Optional.ofNullable(vector3).map(VectorCommand::ordinal).orElse(0);
            if ((section1 + section2 + section3) == 0) {
                // Can only write a 0 byte to terminate shape
                if (vector2 == null) {
                    vector2 = VectorCommand.MOVE_LEFT;
                    section2 = vector2.ordinal();
                } else if (vector3 == null) {
                    vector3 = VectorCommand.MOVE_LEFT;
                    section3 = vector3.ordinal();
                    if (!work.isEmpty()) {
                        work.addFirst(VectorCommand.MOVE_RIGHT);
                    }
                } else {
                    work.addFirst(vector3);
                    vector3 = VectorCommand.MOVE_LEFT;
                    section3 = vector3.ordinal();
                    if (!work.isEmpty()) {
                        work.addFirst(VectorCommand.MOVE_RIGHT);
                    }
                }
            } else if (vector3 == VectorCommand.MOVE_UP) {
                // section 3 cannot be 0
                work.addFirst(vector3);
                vector3 = null;
                if (vector2 == VectorCommand.MOVE_UP) {
                    // section 2 and 3 cannot be 0
                    work.addFirst(vector2);
                    vector2 = null;
                }
            }
            outputStream.write(section3 << 6 | section2 << 3 | section1);
	    }
	    outputStream.write(0);
	    return outputStream.toByteArray();
	}
	
	@Override
	public boolean isEmpty() {
	    return vectors.isEmpty();
	}

	@Override
	public BitmapShape toBitmap() {
	    BitmapShape shape = new BitmapShape();
	    
	    int x = 0;
	    int y = 0;
	    for (VectorCommand command : vectors) {
	        if (command.plot) {
                while (y < 0) {
                    shape.insertRow();
                    y += 1;
                } 
                while (y >= shape.getHeight()) {
                    shape.addRow();
                }
	            while (x < 0) {
	                shape.insertColumn();
	                x += 1;
	            } 
	            while (x >= shape.getWidth()) {
	                shape.addColumn();
	            }
	            shape.plot(x,y);
	        }
	        x += command.xmove;
	        y += command.ymove;
	    }
	    
		return shape;
	}

	@Override
	public VectorShape toVector() {
		return this;
	}
}
