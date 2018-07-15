package io.github.applecommander.bastools.api.shapes;

/** 
 * Represents a single Applesoft shape.  Note that the interface is mostly useful to get at the
 * bitmap or vector shapes.  This also implies that these implementations need to transform between
 * eachother!
 * 
 * @see BitmapShape
 * @see VectorShape
 */
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
