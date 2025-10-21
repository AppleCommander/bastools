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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

public class ShapeGenerator {
    public static ShapeTable generate(Reader sourceReader) throws IOException {
        Objects.requireNonNull(sourceReader);
        
        ShapeTable st = new ShapeTable();
        LineNumberReader reader = new LineNumberReader(sourceReader);
        String line = reader.readLine();
        Consumer<String> shapeConsumer = null;
        while (line != null) {
            int comment = line.indexOf(';');
            if (comment > -1) line = line.substring(0, comment);
            line = line.trim();
            
            String[] parts = line.split("\\s+");
            String command = parts[0];
            String label = parts.length > 1 ? parts[1] : null;
            
            switch (command.toLowerCase()) {
            case ".short":
                VectorShape shortShape = new VectorShape(label);
                st.shapes.add(shortShape);
                shapeConsumer = shortShape::appendShortCommands;
                break;
            case ".long":
                VectorShape longShape = new VectorShape(label);
                st.shapes.add(longShape);
                shapeConsumer = longShape::appendLongCommands;
                break;
            case ".bitmap":
                BitmapShape bitmapShape = new BitmapShape(label);
                st.shapes.add(bitmapShape);
                shapeConsumer = bitmapShape::appendBitmapRow;
                break;
            case ".external":
                ExternalShapeImporter importer = new ExternalShapeImporter(st, label);
                shapeConsumer = importer::process;
                break;
            default:
                if (line.isEmpty()) {
                    // do nothing
                } else if (shapeConsumer != null) {
                    try {
                        shapeConsumer.accept(line);
                    } catch (Throwable t) {
                        String message = String.format("Error at line #%d - %s", reader.getLineNumber(), t.getMessage());
                        throw new IOException(message, t);
                    }
                } else {
                    throw new IOException("Unexpected command: " + line);
                }
                break;
            }
            line = reader.readLine();
        }
        return st;
    }
    public static ShapeTable generate(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream);
        try (Reader reader = new InputStreamReader(inputStream)) {
            return generate(reader);
        }
    }
    public static ShapeTable generate(File file) throws IOException {
        Objects.requireNonNull(file);
        try (Reader reader = new FileReader(file)) {
            return generate(reader);
        }
    }
    public static ShapeTable generate(Path path) throws IOException {
        Objects.requireNonNull(path);
        return generate(path.toFile());
    }
}
