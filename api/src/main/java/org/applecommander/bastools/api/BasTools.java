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
		String title = BasTools.class.getPackage().getImplementationTitle();
		if (title == null) {
			title = "B/BAS Tools API";
		}
		TITLE = title;

		String version = BasTools.class.getPackage().getImplementationVersion();
		if (version == null && BasTools.class.getModule().getDescriptor() != null) {
			// When run under the Java Module system (JPMS) we need to get it a different way.
			version = BasTools.class.getModule()
					.getDescriptor()
					.version()
					.map(Object::toString)
					.orElse("Development Build");
		}
		// For unit tests, the descriptor (apparently) doesn't exist, so we skip it.
		if (version == null) {
			version = "Unit Testing";
		}
		VERSION = version;
	}	
	
	private BasTools() {}
}
