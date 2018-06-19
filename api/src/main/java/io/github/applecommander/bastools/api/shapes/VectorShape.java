package io.github.applecommander.bastools.api.shapes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
	
	public VectorShape moveUp()    { return add(VectorCommand.MOVE_UP);    } 
	public VectorShape moveRight() { return add(VectorCommand.MOVE_RIGHT); } 
	public VectorShape moveDown()  { return add(VectorCommand.MOVE_DOWN);  } 
	public VectorShape moveLeft()  { return add(VectorCommand.MOVE_LEFT);  } 
	public VectorShape plotUp()    { return add(VectorCommand.PLOT_UP);    } 
	public VectorShape plotRight() { return add(VectorCommand.PLOT_RIGHT); } 
	public VectorShape plotDown()  { return add(VectorCommand.PLOT_DOWN);  } 
	public VectorShape plotLeft()  { return add(VectorCommand.PLOT_LEFT);  }
	
	private VectorShape add(VectorCommand vectorCommand) {
		this.vectors.add(vectorCommand);
		return this;
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
