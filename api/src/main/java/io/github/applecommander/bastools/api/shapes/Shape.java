package io.github.applecommander.bastools.api.shapes;

public interface Shape {
    public BitmapShape toBitmap();
	public VectorShape toVector();
}
