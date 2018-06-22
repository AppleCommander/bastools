package io.github.applecommander.bastools.api.shapes;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class BitmapShape implements Shape {
    public final List<List<Boolean>> grid = new ArrayList<>();
    public final Point origin = new Point();
    
    public BitmapShape() {
        this(0,0);
    }
    public BitmapShape(int height, int width) {
        while (grid.size() < height) {
            grid.add(newRow(width));
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
        origin.y++;
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
        origin.x++;
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
            switch (pixel) {
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
        boolean isEmpty = false;
        for (List<Boolean> row : grid) {
            for (Boolean plot : row) {
                isEmpty |= plot;
            }
        }
        return isEmpty;
    }

    @Override
    public BitmapShape toBitmap() {
        return this;
    }

    @Override
    public VectorShape toVector() {
        VectorShape vshape = new VectorShape();
        int width = getWidth();
        int height = getHeight();
        Point pt = new Point(origin);
        // Relocate to 0,0
        while (pt.y > 0) {
            vshape.moveUp();
            pt.y -= 1;
        }
        while (pt.x > 0) {
            vshape.moveLeft();
            pt.x -= 1;
        }
        VectorMotion motion = new RightVectorMotion(pt, vshape);
        while (pt.y >= 0 && pt.y < height) {
            while (pt.x >= 0 && pt.x < width) {
                if (get(pt)) {
                    motion.plot();
                } else {
                    motion.move();
                }
            }
            motion = motion.changeDirection();
        }
        return vshape;
    }
    
//    public static class Whatever {
//        private VectorCommand movement;
//        private VectorCommand nextRow;
//        private Point point;
//        private BitmapShape bitmapShape;
//        private VectorShape vectorShape;
//        
//        public Whatever(VectorCommand movement, VectorCommand nextRow, Point point, BitmapShape bitmapShape) {
//            this.movement = movement;
//            this.nextRow = nextRow;
//            this.point = point;
//            this.bitmapShape = bitmapShape;
//            this.vectorShape = new VectorShape();
//        }
//        
//        public VectorShape transform() {
//            findStartPosition();
//            while (hasMoreRows()) {
//                scanRow();
//            }
//            return vectorShape;
//        }
//    }
    
    public interface VectorMotion {
        public void move();
        public void plot();
        public VectorMotion changeDirection();
    }
    public static class RightVectorMotion implements VectorMotion {
        private Point point;
        private VectorShape vshape;
        public RightVectorMotion(Point point, VectorShape vshape) {
            this.point = point;
            this.vshape = vshape;
        }
        @Override
        public void move() {
            point.x += 1;
            vshape.moveRight();
        }
        @Override
        public void plot() {
            point.x += 1;
            vshape.plotRight();
        }
        @Override
        public VectorMotion changeDirection() {
            point.x -= 1;
            point.y += 1;
            vshape.moveDown();
            vshape.moveLeft();
            return new LeftVectorMotion(point, vshape);
        }
    }
    public static class LeftVectorMotion implements VectorMotion {
        private Point point;
        private VectorShape vshape;
        public LeftVectorMotion(Point point, VectorShape vshape) {
            this.point = point;
            this.vshape = vshape;
        }
        @Override
        public void move() {
            point.x -= 1;
            vshape.moveLeft();
        }
        @Override
        public void plot() {
            point.x -= 1;
            vshape.plotLeft();
        }
        @Override
        public VectorMotion changeDirection() {
            point.x += 1;
            point.y += 1;
            vshape.moveDown();
            vshape.moveRight();
            return new RightVectorMotion(point, vshape);
        }
    }
}
