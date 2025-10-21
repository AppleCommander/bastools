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
package org.applecommander.bastools.api.shapes.exporters;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.applecommander.bastools.api.shapes.BitmapShape;
import org.applecommander.bastools.api.shapes.Shape;
import org.applecommander.bastools.api.shapes.ShapeExporter;
import org.applecommander.bastools.api.shapes.ShapeTable;

public class ImageShapeExporter implements ShapeExporter {
    private int maxWidth = 1024;
    private int pixelSize = 4;
    private final int padding = 2;
    private boolean border = true;
    private boolean skipEmptyShapes;
    private String imageFormat = "PNG";

    /** Use the {@code Builder} to create a ImageShapeExporter. */
    private ImageShapeExporter() { }
    
    @Override
    public void export(Shape shape, OutputStream outputStream) throws IOException {
        Objects.requireNonNull(shape);
        Objects.requireNonNull(outputStream);
        
        export(Arrays.asList(shape.toBitmap()), outputStream);
    }

    @Override
    public void export(ShapeTable shapeTable, OutputStream outputStream) throws IOException {
        Objects.requireNonNull(shapeTable);
        Objects.requireNonNull(outputStream);
        
        List<BitmapShape> blist = shapeTable.shapes.stream()
                                                   .filter(this::displayThisShape)
                                                   .map(Shape::toBitmap)
                                                   .collect(Collectors.toList());
        export(blist, outputStream);
    }
    
    public void export(List<BitmapShape> blist, OutputStream outputStream) throws IOException {
        Objects.requireNonNull(blist);
        Objects.requireNonNull(outputStream);
        
        int shapeWidth = pixelSize * blist.stream().mapToInt(BitmapShape::getWidth).max().getAsInt();
        int shapeHeight = pixelSize * blist.stream().mapToInt(BitmapShape::getHeight).max().getAsInt();
        int borderDividerWidth = border ? 1+padding*2 : 0;
        int borderEdgeWidth = border ? 1+padding : 0;
        
        int columns = Math.min(blist.size(), Math.max(1, this.maxWidth / shapeWidth));
        int rows = (blist.size() + columns - 1) / columns;
        int imageWidth = borderEdgeWidth*2 + columns*shapeWidth + (columns-1)*borderDividerWidth;
        int imageHeight = borderEdgeWidth*2 + rows*shapeHeight + (rows-1)*borderDividerWidth;
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        
        Queue<BitmapShape> bqueue = new LinkedList<>(blist);
        Graphics g = image.createGraphics();
        if (border) drawBorders(g, shapeWidth, shapeHeight, imageWidth, imageHeight);
        Point pt = new Point(borderEdgeWidth, borderEdgeWidth);
        while (!bqueue.isEmpty()) {
            BitmapShape bshape = bqueue.remove();
            drawShapeAt(g, bshape, pt);
            pt.x += shapeWidth + borderDividerWidth;
            if (pt.x > imageWidth) {
                pt.y += shapeHeight + borderDividerWidth;
                pt.x = borderEdgeWidth;
            }
        }
        g.dispose();
        
        ImageIO.write(image, imageFormat, outputStream);
    }

    private boolean displayThisShape(Shape shape) {
        return !(skipEmptyShapes && shape.isEmpty());
    }

    public void drawBorders(Graphics g, int shapeWidth, int shapeHeight, int imageWidth, int imageHeight) {
        g.setColor(Color.white);
        int paddingWidth = border ? padding : 0;
        for (int x=0; x<imageWidth; x+=shapeWidth + paddingWidth*2 + 1) {
            for (int y=0; y<imageHeight; y+=shapeHeight + paddingWidth*2 + 1) {
                g.drawLine(x, 0, x, imageHeight);
                g.drawLine(0, y, imageWidth, y);
            }
        }
    }
    
    public void drawShapeAt(Graphics g, BitmapShape shape, Point origin) {
        for (int x=0; x<shape.getWidth(); x++) {
            for (int y=0; y<shape.getHeight(); y++) {
                g.setColor(shape.get(x, y) ? Color.white : Color.lightGray);
                g.fillRect(origin.x + (x*pixelSize), origin.y + (y*pixelSize), 
                           pixelSize, pixelSize);
            }
        }
    }

    public static class Builder {
        private final ImageShapeExporter shapeExporter = new ImageShapeExporter();
        
        public Builder maxWidth(int maxWidth) {
            shapeExporter.maxWidth = maxWidth;
            return this;
        }
        public Builder pixelSize(int pixelSize) {
            shapeExporter.pixelSize = pixelSize;
            return this;
        }
        public Builder border(boolean border) {
            shapeExporter.border = border;
            return this;
        }
        
        public Builder jpeg() {
            return imageFormat("JPEG");
        }
        public Builder png() {
            return imageFormat("PNG");
        }
        public Builder bmp() {
            return imageFormat("BMP");
        }
        public Builder wbmp() {
            return imageFormat("WBMP");
        }
        public Builder gif() {
            return imageFormat("GIF");
        }
        public Builder imageFormat(String imageFormat) {
            shapeExporter.imageFormat = imageFormat;
            return this;
        }
        
        public Builder skipEmptyShapes() {
            return skipEmptyShapes(true);
        }
        public Builder skipEmptyShapes(boolean skipEmptyShapes) {
            shapeExporter.skipEmptyShapes = skipEmptyShapes;
            return this;
        }
        
        public ShapeExporter build() {
            return shapeExporter;
        }
    }
}
