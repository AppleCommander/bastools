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
    
    @Test
    public void testOpposite() {
        test(VectorCommand.MOVE_DOWN, VectorCommand.MOVE_UP);
        test(VectorCommand.MOVE_LEFT, VectorCommand.MOVE_RIGHT);
        test(VectorCommand.PLOT_DOWN, VectorCommand.PLOT_UP);
        test(VectorCommand.PLOT_LEFT, VectorCommand.PLOT_RIGHT);
    }
    public void test(VectorCommand a, VectorCommand b) {
        assertEquals(a, b.opposite());
        assertEquals(b, a.opposite());
    }
}
