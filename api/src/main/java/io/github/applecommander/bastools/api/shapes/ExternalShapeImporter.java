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
package io.github.applecommander.bastools.api.shapes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

import io.github.applecommander.bastools.api.utils.Converters;

/**
 * Allow the import of an external shape.  Processing is very dependent on
 * being invoked in the "correct" manner!
 * <p>
 * Prototype code:
 * <pre>
 * ; Read in external shape table: configure first and then "import" processes file.
 * .external characters
 *   type=bin
 *   shapes=1-96
 *   import=imperator.bin
 * </pre>
 */
public class ExternalShapeImporter {
    private final ShapeTable destination;
    private String firstShapeLabel;
    private Function<String,ShapeTable> importer = this::importShapeTableFromBinary;
    private IntStream intStream = null;
    
    public ExternalShapeImporter(ShapeTable destination, String firstShapeLabel) {
        this.destination = destination;
        this.firstShapeLabel = firstShapeLabel;
    }
    
    public void process(String line) {
        Objects.requireNonNull(line);
        String[] parts = line.split("=");
        if (parts.length != 2) {
            throw new RuntimeException(String.format(".external fields require an assignment for '%s'", line));
        }
        switch (parts[0].toLowerCase()) {
        case "type":
            switch (parts[1].toLowerCase()) {
            case "bin":
                importer = this::importShapeTableFromBinary;
                break;
            case "src":
                importer = this::importShapeTableFromSource;
                break;
            default:
                throw new RuntimeException(String.format("Unknown import type specified: '%s'", line));
            }
            break;
        case "shapes":
            intStream = Converters.toIntStream(parts[1]);
            break;
        case "import":
            ShapeTable temp = importer.apply(parts[1]);
            // Shapes in Applesoft are 1 based but Java List object is 0 based...
            intStream.map(n -> n-1).mapToObj(temp.shapes::get).forEach(this::importShape);
            break;
        default:
            throw new RuntimeException(String.format("Unknown assignment '%s' for .external", line));
        }
    }
    
    public ShapeTable importShapeTableFromBinary(String filename) {
        // FIXME May need access to Configuration for these nested files?
        try {
            Objects.requireNonNull(intStream, ".external requires that 'shapes' is specified");
            return ShapeTable.read(Paths.get(filename));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    public ShapeTable importShapeTableFromSource(String filename) {
        // FIXME May need access to Configuration for these nested files?
        try {
            Objects.requireNonNull(intStream, ".external requires that 'shapes' is specified");
            return ShapeGenerator.generate(Paths.get(filename));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    public void importShape(Shape shape) {
        if (firstShapeLabel != null) {
            VectorShape vshape = new VectorShape(firstShapeLabel);
            vshape.vectors.addAll(shape.toVector().vectors);
            destination.shapes.add(vshape);
            firstShapeLabel = null;
        } else {
            destination.shapes.add(shape);
        }
    }
}
