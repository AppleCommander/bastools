package io.github.applecommander.bastools.api.shapes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VectorCommandTest {
    @Test
    public void testDirections() {
        test(0, 1, VectorCommand.MOVE_DOWN, VectorCommand.PLOT_DOWN);
        test(0, -1, VectorCommand.MOVE_UP, VectorCommand.PLOT_UP);
        test(-1, 0, VectorCommand.MOVE_LEFT, VectorCommand.PLOT_LEFT);
        test(1, 0, VectorCommand.MOVE_RIGHT, VectorCommand.PLOT_RIGHT);
    }
    public void test(int xmove, int ymove, VectorCommand... commands) {
        for (VectorCommand command : commands) {
            assertEquals(xmove, command.xmove);
            assertEquals(ymove, command.ymove);
        }
    }
    
    @Test
    public void testPlot() {
        test(false, VectorCommand.MOVE_DOWN, VectorCommand.MOVE_LEFT, VectorCommand.MOVE_RIGHT, VectorCommand.MOVE_UP);
        test(true, VectorCommand.PLOT_DOWN, VectorCommand.PLOT_LEFT, VectorCommand.PLOT_RIGHT, VectorCommand.PLOT_UP);
    }
    public void test(boolean plot, VectorCommand... commands) {
        for (VectorCommand command : commands) {
            assertEquals(plot, command.plot);
        }
    }
}
