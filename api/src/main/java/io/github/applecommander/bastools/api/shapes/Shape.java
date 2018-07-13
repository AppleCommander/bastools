package io.github.applecommander.bastools.api.shapes;

public interface Shape {
    /** Indicates if this shape is empty. */
    public boolean isEmpty();
    /** Get the label of this shape. */
    public String getLabel();
    /** Transform to a BitmapShape. */
    public BitmapShape toBitmap();
    /** Transform to a VectorShape. */
	public VectorShape toVector();
}
