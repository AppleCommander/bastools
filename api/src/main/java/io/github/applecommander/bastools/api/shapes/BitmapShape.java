package io.github.applecommander.bastools.api.shapes;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents a bitmap copy of the shape.
 * This may be useful for displaying the shape or for defining shapes as a bitmap is 
 * easier to understand than vectors.
 */
public class BitmapShape implements Shape {
    public final String label;
    public final List<List<Boolean>> grid = new ArrayList<>();
    public final Point origin = new Point();
    
    public BitmapShape() {
        this(0, 0, null);
    }
    public BitmapShape(String label) {
        this(0, 0, label);
    }
    public BitmapShape(int height, int width) {
        this(height, width, null);
    }
    public BitmapShape(int height, int width, String label) {
        this.label = label;
        while (this.grid.size() < height) {
            this.grid.add(newRow(width));
        }
    }
    
    private List<Boolean> newRow(int width) {
        List<Boolean> row = new ArrayList<>();
        while (row.size() < width) {
            row.add(Boolean.FALSE);
        }
        return row;
    }
    
    public void insertColumn() {
        origin.x++;
        for (List<Boolean> row : grid) {
            row.add(0, Boolean.FALSE);
        }
    }
    public void addColumn() {
        for (List<Boolean> row : grid) {
            row.add(Boolean.FALSE);
        }
    }
    public void insertRow() {
        origin.y++;
        grid.add(0, newRow(getWidth()));
    }
    public void addRow() {
        grid.add(newRow(getWidth()));
    }
    
    public void appendBitmapRow(String line) {
        line = line.trim();
        List<Boolean> row = new ArrayList<>();
        Runnable setOrigin = () -> {
                // Share origin logic for '+' and '*'
                origin.x = row.size();
                origin.y = grid.size();
            };
        for (char pixel : line.toCharArray()) {
            switch (Character.toLowerCase(pixel)) {
            case '+':
                setOrigin.run();
                // fall through to '.'
            case '.':
                row.add(Boolean.FALSE);
                break;
            case '*':
                setOrigin.run();
                // fall through to 'x'
            case 'x':
                row.add(Boolean.TRUE);
                break;
            default:
                throw new RuntimeException("Unexpected bitmap pixel type: " + pixel);
            }
        }
        grid.add(row);
    }
    
    public int getHeight() {
        return grid.size(); 
    }
    public int getWidth() {
        return grid.isEmpty() ? 0 : grid.get(0).size();
    }
    
    public void plot(int x, int y) {
        plot(x, y, Boolean.TRUE);
    }
    public void plot(int x, int y, Boolean pixel) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return; 
        }
        grid.get(y).set(x, pixel);
    }
    
    public Boolean get(int x, int y) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
            return Boolean.FALSE; 
        }
        return grid.get(y).get(x);
    }
    public Boolean get(Point point) {
        return get(point.x, point.y);
    }
    
    @Override
    public boolean isEmpty() {
        boolean hasData = false;
        for (List<Boolean> row : grid) {
            for (Boolean plot : row) {
                hasData |= plot;
            }
        }
        return !hasData;
    }
    
    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public BitmapShape toBitmap() {
        return this;
    }

    /**
     * Convert this bitmap shape to a vector shape.  The shape chosen encodes to the least number of bytes
     * in the resulting file.
     */
    @Override
    public VectorShape toVector() {
        List<Supplier<VectorShape>> scans = Arrays.asList(
                new SweepVectorization(this, VectorCommand.MOVE_RIGHT, VectorCommand.MOVE_UP),
                new SweepVectorization(this, VectorCommand.MOVE_RIGHT, VectorCommand.MOVE_DOWN),
                new SweepVectorization(this, VectorCommand.MOVE_LEFT, VectorCommand.MOVE_UP),
                new SweepVectorization(this, VectorCommand.MOVE_LEFT, VectorCommand.MOVE_DOWN),
                new SweepVectorization(this, VectorCommand.MOVE_DOWN, VectorCommand.MOVE_RIGHT),
                new SweepVectorization(this, VectorCommand.MOVE_DOWN, VectorCommand.MOVE_LEFT),
                new SweepVectorization(this, VectorCommand.MOVE_UP, VectorCommand.MOVE_RIGHT),
                new SweepVectorization(this, VectorCommand.MOVE_UP, VectorCommand.MOVE_LEFT),
                new EuclidianDistanceVectorization(this)
            );
        
        int byteLength = Integer.MAX_VALUE;
        VectorShape vshape = null;
        for (Supplier<VectorShape> scan : scans) {
            VectorShape candidate = scan.get();
            int length = candidate.toBytes().length;
            if (vshape == null || byteLength >= length) {
                vshape = candidate;
                byteLength = length;
            }
        }
        return vshape;
    }
    
    /**
     * Encode a bitmap shape by going to a corner and sweeping back-and-forth across the image.
     * The resulting shape is not optimal, so the {@link VectorShape#optimize()} should be used.
     * Note that this class is setup to be dynamic in the chosen corner.
     */
    public static class SweepVectorization implements Supplier<VectorShape> {
        private final VectorCommand[] toOrigin;
        private VectorCommand movement;
        private final VectorCommand next;
        private final Point point;
        private final BitmapShape bitmapShape;
        private final VectorShape vectorShape;
        private final int width;
        private final int height;
        
        /**
         * Create an instance of the sweep method.
         * 
         * @param bitmapShape is the shape to be converted
         * @param initialMovement is the initial sweep movement
         * @param next is the direction to advance for each line
         */
        public SweepVectorization(BitmapShape bitmapShape, VectorCommand initialMovement, VectorCommand next) {
            Objects.requireNonNull(bitmapShape);
            Objects.requireNonNull(initialMovement);
            Objects.requireNonNull(next);
            if (initialMovement.horizontal == next.horizontal || initialMovement.vertical == next.vertical) {
                throw new IllegalArgumentException("One vector must be horizontal and the other vector must be vertical");
            }
            
            this.toOrigin = new VectorCommand[] { next.opposite(), initialMovement.opposite() };
            this.movement = initialMovement;
            this.next = next;
            this.bitmapShape = bitmapShape;
            this.width = bitmapShape.getWidth();
            this.height = bitmapShape.getHeight();
            this.point = new Point(bitmapShape.origin);
            this.vectorShape = new VectorShape();
        }
        
        public VectorShape get() {
            findStartPosition();
            while (!onOrAtEdge(next)) {
                scanRow();
                plotOrMove(next);
                movement = movement.opposite();
                point.translate(next.xmove, next.ymove);
            }
            return vectorShape;
        }
        
        public void findStartPosition() {
            for (VectorCommand vector : toOrigin) {
                while (!onOrAtEdge(vector)) {
                    vectorShape.vectors.add(vector);
                    point.translate(vector.xmove, vector.ymove);
                }
            }
        }
        
        public void scanRow() {
            while (!onOrAtEdge(movement)) {
                plotOrMove(movement);
                point.translate(movement.xmove, movement.ymove);
            }
        }
        
        public void plotOrMove(VectorCommand vector) {
            if (bitmapShape.get(point)) {
                vectorShape.appendShortCommand(Character.toUpperCase(vector.shortCommand));
            } else {
                vectorShape.appendShortCommand(Character.toLowerCase(vector.shortCommand));
            }
        }
        
        public boolean onOrAtEdge(VectorCommand vector) {
            // No clever way to do this?
            switch (vector) {
            case MOVE_DOWN:
            case PLOT_DOWN:
                return point.y >= height;
            case MOVE_UP:
            case PLOT_UP:
                return point.y < 0;
            case MOVE_LEFT:
            case PLOT_LEFT:
                return point.x < 0;
            case MOVE_RIGHT:
            case PLOT_RIGHT:
                return point.x >= width;
            default:
                throw new RuntimeException("Unexpected vector: " + vector);
            }
        }
    }

    /**
     * Encode a bitmap shape by using the Euclidean distance between plotted points to determine
     * which vectors take precedence.  Most of the math is captured in the Point class, which makes
     * this much more straight-forward.
     */
    public static class EuclidianDistanceVectorization implements Supplier<VectorShape> {
        private final List<Point> points;
        private final BitmapShape bitmapShape;
        private final VectorShape vshape;
        
        public EuclidianDistanceVectorization(BitmapShape bitmapShape) {
            this.bitmapShape = bitmapShape;
            this.points = new ArrayList<>();
            this.vshape = new VectorShape();
            // Collect vector targets
            for (int y=0; y<bitmapShape.getHeight(); y++) {
                for (int x=0; x<bitmapShape.getWidth(); x++) {
                    if (bitmapShape.get(x,y)) {
                        points.add(new Point(x,y));
                    }
                }
            }
        }

        @Override
        public VectorShape get() {
            Point point = new Point(bitmapShape.origin);
            boolean plotFirst = false;
            while (!points.isEmpty()) {
                Point target = null;
                double distance = Double.MAX_VALUE;
                for (Point candidate : points) {
                    double candidateDistance = point.distance(candidate);
                    if (distance >= candidateDistance) {
                        distance = candidateDistance;
                        target = candidate;
                    }
                }
                moveTo(plotFirst, point, target);
                points.remove(target);
                point = target;
                plotFirst = true;
            }
            // Need to plot that last pixel
            vshape.plotUp();
            return vshape;
        }
        
        /**
         * Move from origin to target.  General strategy is to work from distance between points and bias the one that "loses"
         * each time while resetting the one that "won".  Hopefully this draws more of a jagged line than something entirely
         * straight.  (The goal being to prevent a bunch of plot up's in the worst case that doesn't encode nicely.)
         * @param plotFirst Indicates if the first vector needs to plot; otherwise all other vectors will be moves
         * @param origin Is the point of origin (which can be a plotted pixel)
         * @param target Is the target point 
         */
        public void moveTo(boolean plotFirst, Point origin, Point target) {
            if (origin.equals(target)) return;
            VectorCommand xvector = origin.x < target.x ? VectorCommand.MOVE_RIGHT : VectorCommand.MOVE_LEFT;
            VectorCommand yvector = origin.y < target.y ? VectorCommand.MOVE_DOWN : VectorCommand.MOVE_UP;
            if (plotFirst) {
                xvector = xvector.plot();
                yvector = yvector.plot();
            }
            Point point = new Point(origin);
            int xdist = Math.abs(point.x - target.x);
            int ydist = Math.abs(point.y - target.y);
            while (!point.equals(target)) {
                if (xdist > ydist) {
                    xdist = Math.abs(point.x - target.x);
                    ydist += 1;
                    if (point.x != target.x) {
                        point.translate(xvector.xmove, xvector.ymove);
                        vshape.append(xvector);
                        xvector = xvector.move();
                        yvector = yvector.move();
                    }
                } else {
                    xdist += 1;
                    ydist = Math.abs(point.y - target.y);
                    if (point.y != target.y) {
                        point.translate(yvector.xmove, yvector.ymove);
                        vshape.append(yvector);
                        xvector = xvector.move();
                        yvector = yvector.move();
                    }
                }
            }
        }
    }
}
