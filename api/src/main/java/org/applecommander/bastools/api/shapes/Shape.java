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
    boolean isEmpty();
    /** Get the label of this shape. */
    String getLabel();
    /** Transform to a BitmapShape. */
    BitmapShape toBitmap();
    /** Transform to a VectorShape. */
    VectorShape toVector();
}
