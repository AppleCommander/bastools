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
package org.applecommander.bastools.api;

/**
 * Since there are many pieces to bastools, the version information is just a small, 
 * dedicated class.
 */
public class BasTools {
	public static final String VERSION;
	public static final String TITLE;
	static {
		TITLE = BasTools.class.getPackage().getImplementationTitle();
		VERSION = BasTools.class.getPackage().getImplementationVersion();
	}	
	
	private BasTools() {}
}
