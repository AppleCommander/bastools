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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.applecommander.bastools.api.shapes.exporters.ImageShapeExporter;
import org.applecommander.bastools.api.shapes.exporters.SourceShapeExporter;
import org.applecommander.bastools.api.shapes.exporters.TextShapeExporter;

public interface ShapeExporter {
    /** Export a single shape to the OutputStream. */
    void export(Shape shape, OutputStream outputStream) throws IOException;
    /** Export a single shape to the File. */
    default void export(Shape shape, File file) throws IOException {
        Objects.requireNonNull(shape);
        Objects.requireNonNull(file);
        export(shape, file.toPath());
    }
    /** Export a single shape to the Path. */
    default void export(Shape shape, Path path) throws IOException {
        Objects.requireNonNull(shape);
        Objects.requireNonNull(path);
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            export(shape, outputStream);
        }
    }

    /** Export the entire shape table to the OutputStream. */
    void export(ShapeTable shapeTable, OutputStream outputStream) throws IOException;
    /** Export the entire shape table to the File. */
    default void export(ShapeTable shapeTable, File file) throws IOException {
        Objects.requireNonNull(shapeTable);
        Objects.requireNonNull(file);
        export(shapeTable, file.toPath());
    }
    /** Export the entire shape table to the Path. */
    default void export(ShapeTable shapeTable, Path path) throws IOException {
        Objects.requireNonNull(shapeTable);
        Objects.requireNonNull(path);
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            export(shapeTable, outputStream);
        }
    }
    
    static TextShapeExporter.Builder text() {
        return new TextShapeExporter.Builder();
    }
    static ImageShapeExporter.Builder image() {
        return new ImageShapeExporter.Builder();
    }
    static SourceShapeExporter.Builder source() {
        return new SourceShapeExporter.Builder();
    }
}
