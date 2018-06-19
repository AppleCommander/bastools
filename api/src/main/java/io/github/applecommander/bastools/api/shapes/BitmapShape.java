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
        // TODO Auto-generated method stub
        return null;
    }
}
