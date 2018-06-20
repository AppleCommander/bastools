package io.github.applecommander.bastools.api.shapes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.applecommander.bastools.api.utils.Streams;

public class ShapeTable {
    public static ShapeTable read(byte[] data) {
        Objects.requireNonNull(data);
        ShapeTable shapeTable = new ShapeTable();
        ByteBuffer buf = ByteBuffer.wrap(data)
                                   .order(ByteOrder.LITTLE_ENDIAN);
        int count = Byte.toUnsignedInt(buf.get());
        // unused:
        buf.get();
        for (int i = 0; i < count; i++) {
            int offset = buf.getShort();
            // load empty shapes as empty...
            if (offset == 0) {
                shapeTable.shapes.add(new VectorShape());
                continue;
            }
            // defer to VectorShape to process bits
            buf.mark();
            buf.position(offset);
            shapeTable.shapes.add(VectorShape.from(buf));
            buf.reset();
        }
        return shapeTable;
    }

    public static ShapeTable read(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream);
        return read(Streams.toByteArray(inputStream));
    }

    public static ShapeTable read(File file) throws IOException {
        Objects.requireNonNull(file);
        return read(file.toPath());
    }

    public static ShapeTable read(Path path) throws IOException {
        Objects.requireNonNull(path);
        return read(Files.readAllBytes(path));
    }

    public final List<Shape> shapes = new ArrayList<>();

    public void write(OutputStream outputStream) throws IOException {
        Objects.requireNonNull(outputStream);
        // Header
        outputStream.write(shapes.size());
        outputStream.write(0);
        // Collect each shape
        List<byte[]> data = this.shapes.stream()
                                       .map(Shape::toVector)
                                       .map(VectorShape::toBytes)
                                       .collect(Collectors.toList());
        // Build offset table
        int offset = 2 + 2*data.size();
        for (byte[] d : data) {
            outputStream.write(offset & 0xff);
            outputStream.write(offset >> 8);
            offset += d.length;
        }
        // Write shape data
        for (byte[] d : data) {
            outputStream.write(d);
        }
    }
    public void write(File file) throws IOException {
        Objects.requireNonNull(file);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            write(file);
        }
    }
    public void write(Path path) throws IOException {
        Objects.requireNonNull(path);
        write(path.toFile());
    }
}
