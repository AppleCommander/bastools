package io.github.applecommander.bastools.api.shapes;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import io.github.applecommander.bastools.api.shapes.exporters.TextShapeExporter;

public interface ShapeExporter {
    /** Export a single shape to the OutputStream. */
    public void export(Shape shape, OutputStream outputStream);
    /** Export a single shape to the File. */
    public default void export(Shape shape, File file) throws IOException {
        Objects.requireNonNull(shape);
        Objects.requireNonNull(file);
        export(shape, file.toPath());
    }
    /** Export a single shape to the Path. */
    public default void export(Shape shape, Path path) throws IOException {
        Objects.requireNonNull(shape);
        Objects.requireNonNull(path);
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            export(shape, outputStream);
        }
    }

    /** Export the entire shape table to the OutputStream. */
    public void export(ShapeTable shapeTable, OutputStream outputStream);
    /** Export the entire shape table to the File. */
    public default void export(ShapeTable shapeTable, File file) throws IOException {
        Objects.requireNonNull(shapeTable);
        Objects.requireNonNull(file);
        export(shapeTable, file.toPath());
    }
    /** Export the entire shape table to the Path. */
    public default void export(ShapeTable shapeTable, Path path) throws IOException {
        Objects.requireNonNull(shapeTable);
        Objects.requireNonNull(path);
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            export(shapeTable, outputStream);
        }
    }
    
    public static TextShapeExporter.Builder text() {
        return new TextShapeExporter.Builder();
    }
}
