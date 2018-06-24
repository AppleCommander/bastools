package io.github.applecommander.bastools.api.shapes.exporters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiConsumer;

import io.github.applecommander.bastools.api.shapes.Shape;
import io.github.applecommander.bastools.api.shapes.ShapeExporter;
import io.github.applecommander.bastools.api.shapes.ShapeTable;
import io.github.applecommander.bastools.api.shapes.VectorCommand;

public class SourceShapeExporter implements ShapeExporter {
    private BiConsumer<Shape,PrintWriter> formatFunction = this::exportShapeAsBitmap;
    private ShapeExporter textExporter;
    private boolean skipEmptyShapes;

    /** Use the {@code Builder} to create a TextShapeExporter. */
    private SourceShapeExporter() {
        this.textExporter = ShapeExporter.text().noBorder().build();
    }
    
    @Override
    public void export(Shape shape, OutputStream outputStream) throws IOException {
        PrintWriter pw = new PrintWriter(outputStream);
        formatFunction.accept(shape, pw);
        pw.flush();
    }

    @Override
    public void export(ShapeTable shapeTable, OutputStream outputStream) throws IOException {
        PrintWriter pw = new PrintWriter(outputStream);
        shapeTable.shapes.stream()
                         .filter(this::displayThisShape)
                         .forEach(shape -> formatFunction.accept(shape, pw));
        pw.flush();
    }

    private boolean displayThisShape(Shape shape) {
        return !(skipEmptyShapes && shape.isEmpty());
    }

    public void exportShapeAsBitmap(Shape shape, PrintWriter pw) {
        try {
            pw.printf(".bitmap\n");
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            textExporter.export(shape, new PaddedOutputStream(os, "  "));
            pw.print(new String(os.toByteArray()));
            pw.printf("\n");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    public void exportShapeAsShortCommands(Shape shape, PrintWriter pw) {
        pw.printf(".short\n");
        pw.printf("  %s\n", shape.toVector().toShortCommands());
        pw.printf("\n");
    }
    
    public void exportShapeAsLongCommands(Shape shape, PrintWriter pw) {
        pw.printf(".long\n");
        Queue<VectorCommand> vectors = new LinkedList<>(shape.toVector().vectors);
        while (!vectors.isEmpty()) {
            VectorCommand vector = vectors.remove();
            int count = 1;
            while (vectors.peek() == vector) {
                vectors.remove();
                count += 1;
            }
            if (count == 1) {
                pw.printf("  %s\n", vector.longCommand);
            } else {
                pw.printf("  %s %d\n", vector.longCommand, count);
            }
        }
        pw.printf("\n");
    }
    
    public static class Builder {
        private SourceShapeExporter exporter = new SourceShapeExporter();
        
        public Builder bitmap() {
            exporter.formatFunction = exporter::exportShapeAsBitmap;
            return this;
        }
        public Builder shortCommands() {
            exporter.formatFunction = exporter::exportShapeAsShortCommands;
            return this;
        }
        public Builder longCommands() {
            exporter.formatFunction = exporter::exportShapeAsLongCommands;
            return this;
        }
        
        public Builder skipEmptyShapes() {
            return skipEmptyShapes(true);
        }
        public Builder skipEmptyShapes(boolean skipEmptyShapes) {
            exporter.skipEmptyShapes = skipEmptyShapes;
            return this;
        }
        
        public ShapeExporter build() {
            return exporter;
        }
    }
    
    public static class PaddedOutputStream extends OutputStream {
        private OutputStream wrappedStream;
        private boolean needPadding = true;
        private byte[] padding;
        
        public PaddedOutputStream(OutputStream outputStream, String padding) {
            Objects.requireNonNull(outputStream);
            Objects.requireNonNull(padding);
            this.wrappedStream = outputStream;
            this.padding = padding.getBytes();
        }
        
        @Override
        public void write(int b) throws IOException {
            if (needPadding) {
                wrappedStream.write(padding);
            }
            needPadding = (b == '\n');
            wrappedStream.write(b);
        }
    }
}