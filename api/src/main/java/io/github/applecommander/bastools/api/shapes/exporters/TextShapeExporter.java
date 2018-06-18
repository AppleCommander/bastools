package io.github.applecommander.bastools.api.shapes.exporters;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.github.applecommander.bastools.api.shapes.BitmapShape;
import io.github.applecommander.bastools.api.shapes.Shape;
import io.github.applecommander.bastools.api.shapes.ShapeExporter;
import io.github.applecommander.bastools.api.shapes.ShapeTable;

public class TextShapeExporter implements ShapeExporter {
    private int maxWidth = 80;
    private BorderStrategy borderStrategy = BorderStrategy.BOX_DRAWING;
    
    /** Use the {@code Builder} to create a TextShapeExporter. */
    private TextShapeExporter() { }

    @Override
    public void export(Shape shape, OutputStream outputStream) {
        Objects.requireNonNull(shape);
        Objects.requireNonNull(outputStream);
        
        BitmapShape b = shape.toBitmap();
        PrintWriter pw = new PrintWriter(outputStream);
        drawTopLine(pw, 1, b.getWidth());
        
        Queue<BitmapShape> bqueue = new LinkedList<>(Arrays.asList(b));
        drawRow(pw, bqueue, 1, b.getHeight(), b.getWidth());

        drawBottomLine(pw, 1, b.getWidth());
        pw.flush();
    }

    @Override
    public void export(ShapeTable shapeTable, OutputStream outputStream) {
        Objects.requireNonNull(shapeTable);
        Objects.requireNonNull(outputStream);
        
        List<BitmapShape> blist = shapeTable.shapes.stream()
                                                   .map(Shape::toBitmap)
                                                   .collect(Collectors.toList());
        int width = blist.stream().mapToInt(BitmapShape::getWidth).max().getAsInt();
        int height = blist.stream().mapToInt(BitmapShape::getHeight).max().getAsInt();

        int columns = Math.max(1, this.maxWidth / width);
        
        PrintWriter pw = new PrintWriter(outputStream);
        drawTopLine(pw, columns, width);
        
        Queue<BitmapShape> bqueue = new LinkedList<>(blist);
        drawRow(pw, bqueue, columns, height, width);
        while (!bqueue.isEmpty()) {
            drawDividerLine(pw, columns, width);
            drawRow(pw, bqueue, columns, height, width);
        }
        
        drawBottomLine(pw, columns, width);
        pw.flush();
    }
    
    private void drawTopLine(PrintWriter pw, int columns, int width) {
        borderStrategy.topLeftCorner(pw);
        borderStrategy.horizontalLine(pw, width);
        for (int i=1; i<columns; i++) {
            borderStrategy.topDivider(pw);
            borderStrategy.horizontalLine(pw, width);
        }
        borderStrategy.topRightCorner(pw);
    }
    private void drawDividerLine(PrintWriter pw, int columns, int width) {
        borderStrategy.dividerLeftEdge(pw);
        borderStrategy.dividerHorizontalLine(pw, width);
        for (int i=1; i<columns; i++) {
            borderStrategy.dividerMiddle(pw);
            borderStrategy.dividerHorizontalLine(pw, width);
        }
        borderStrategy.dividerRightEdge(pw);
    }
    private void drawBottomLine(PrintWriter pw, int columns, int width) {
        borderStrategy.bottomLeftCorner(pw);
        borderStrategy.horizontalLine(pw, width);
        for (int i=1; i<columns; i++) {
            borderStrategy.bottomDivider(pw);
            borderStrategy.horizontalLine(pw, width);
        }
        borderStrategy.bottomRightCorner(pw);
    }
    private void drawRow(PrintWriter pw, Queue<BitmapShape> bqueue, int columns, int height, int width) {
        BitmapShape[] bshapes = new BitmapShape[columns];
        for (int i=0; i<bshapes.length; i++) {
            bshapes[i] = bqueue.isEmpty() ? new BitmapShape() : bqueue.remove();
        }

        for (int y=0; y<height; y++) {
            borderStrategy.verticalLine(pw);
            drawRowLine(pw, bshapes[0], width, y);
            for (int c=1; c<bshapes.length; c++) {
                borderStrategy.dividerVerticalLine(pw);
                drawRowLine(pw, bshapes[c], width, y);
            }
            borderStrategy.verticalLine(pw);
            pw.println();
        }
    }
    private void drawRowLine(PrintWriter pw, BitmapShape bshape, int width, int y) {
        List<Boolean> row = bshape.grid.size() > y ? bshape.grid.get(y) : new ArrayList<>();
        for (int x=0; x<width; x++) {
            Boolean plot = row.size() > x ? row.get(x) : Boolean.FALSE;
            if (bshape.origin.x == x && bshape.origin.y == y) {
                pw.printf("%c", plot ? '*' : '+');
            } else {
                pw.printf("%c", plot ? 'X' : '.');
            }
        }
    }

    public enum BorderStrategy {
        /** No border but with spaces between shapes. Note the tricky newline in {@code dividerLeftEdge}. */
        NONE('\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', ' ', '\0', '\n', '\0', '\0'),
        /** 
         * A border comprised of the box drawing characters. 
         * @see <a href='https://en.wikipedia.org/wiki/Box-drawing_character'>Wikipedia article on box characters</a> 
         */
        BOX_DRAWING('\u2500', '\u2502', '\u250C', '\u2510', '\u2514', '\u2518', '\u252C', '\u2534', 
                '\u2502', '\u2500', '\u251C', '\u2524', '\u253C'),
        /** A simple border based on plain ASCII characters. */
        ASCII_TEXT('-', '|', '+', '+', '+', '+', '+', '+', '|', '-', '+', '+', '+');
        
        private final char horizontalLine;
        private final char verticalLine;
        private final char topLeftCorner;
        private final char topRightCorner;
        private final char bottomLeftCorner;
        private final char bottomRightCorner;
        private final char topDivider;
        private final char bottomDivider;
        private final char dividerVerticalLine;
        private final char dividerHorizontalLine;
        private final char dividerLeftEdge;
        private final char dividerRightEdge;
        private final char dividerMiddle;
        
        private BorderStrategy(char horizontalLine, char verticalLine, char topLeftCorner, char topRightCorner,
                char bottomLeftCorner, char bottomRightCorner, char topDivider, char bottomDivider,
                char dividerVerticalLine, char dividerHorizontalLine, char dividerLeftEdge, char dividerRightEdge,
                char dividerMiddle) {
            this.horizontalLine = horizontalLine;
            this.verticalLine = verticalLine;
            this.topLeftCorner = topLeftCorner;
            this.topRightCorner = topRightCorner;
            this.bottomLeftCorner = bottomLeftCorner;
            this.bottomRightCorner = bottomRightCorner;
            this.topDivider = topDivider;
            this.bottomDivider = bottomDivider;
            this.dividerVerticalLine = dividerVerticalLine;
            this.dividerHorizontalLine = dividerHorizontalLine;
            this.dividerLeftEdge = dividerLeftEdge;
            this.dividerRightEdge = dividerRightEdge;
            this.dividerMiddle = dividerMiddle;
        }

        private void print(Consumer<String> output, char ch) {
            print(output, ch, 1);
        }
        private void print(Consumer<String> output, char ch, int width) {
            if (ch != '\0') {
                output.accept(new String(new char[width]).replace('\0', ch));
            }
        }

        public void horizontalLine(PrintWriter pw, int width) {
            print(pw::print, horizontalLine, width);
        }
        public void verticalLine(PrintWriter pw) {
            print(pw::print, verticalLine);
        }
        public void topLeftCorner(PrintWriter pw) {
            print(pw::print, topLeftCorner);
        }
        public void topRightCorner(PrintWriter pw) {
            print(pw::println, topRightCorner);
        }
        public void bottomLeftCorner(PrintWriter pw) {
            print(pw::print, bottomLeftCorner);
        }
        public void bottomRightCorner(PrintWriter pw) {
            print(pw::println, bottomRightCorner);
        }
        public void topDivider(PrintWriter pw) {
            print(pw::print, topDivider);
        }
        public void bottomDivider(PrintWriter pw) {
            print(pw::print, bottomDivider);
        }
        public void dividerVerticalLine(PrintWriter pw) {
            print(pw::print, dividerVerticalLine);
        }
        public void dividerHorizontalLine(PrintWriter pw, int width) {
            print(pw::print, dividerHorizontalLine, width);
        }
        public void dividerLeftEdge(PrintWriter pw) {
            print(pw::print, dividerLeftEdge);
        }
        public void dividerRightEdge(PrintWriter pw) {
            print(pw::println, dividerRightEdge);
        }
        public void dividerMiddle(PrintWriter pw) {
            print(pw::print, dividerMiddle);
        }
    }
    
    public static class Builder {
        private TextShapeExporter textShapeExporter = new TextShapeExporter();
        
        public Builder maxWidth(int maxWidth) {
            textShapeExporter.maxWidth = maxWidth;
            return this;
        }
        public Builder noBorder() {
            return borderStrategy(BorderStrategy.NONE);
        }
        public Builder asciiTextBorder() {
            return borderStrategy(BorderStrategy.ASCII_TEXT);
        }
        public Builder boxDrawingBorder() {
            return borderStrategy(BorderStrategy.BOX_DRAWING);
        }
        public Builder borderStrategy(BorderStrategy borderStrategy) {
            textShapeExporter.borderStrategy = borderStrategy;
            return this;
        }
        
        public ShapeExporter build() {
            return textShapeExporter;
        }
    }
}
