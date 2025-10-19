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
package io.github.applecommander.bastools.api;

import java.io.OutputStream;
import java.io.Serial;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.TreeMap;

import io.github.applecommander.bastools.api.directives.EmbeddedBinaryDirective;
import io.github.applecommander.bastools.api.directives.EmbeddedShapeTable;
import io.github.applecommander.bastools.api.directives.HexDirective;

public class Directives {
	private Directives() { /* Prevent construction. */ }
	
	private static final Map<String,Class<? extends Directive>> DIRECTIVES =
        new TreeMap<>(String.CASE_INSENSITIVE_ORDER) {
            @Serial
            private static final long serialVersionUID = -8111460701487331592L;

            {
                put(EmbeddedBinaryDirective.NAME, EmbeddedBinaryDirective.class);
                put(HexDirective.NAME, HexDirective.class);
                put(EmbeddedShapeTable.NAME, EmbeddedShapeTable.class);
            }
        };

	public static Directive find(String text, Configuration config, OutputStream outputStream) {
		if (DIRECTIVES.containsKey(text)) {
			try {
				Class<? extends Directive> clazz = DIRECTIVES.get(text);
				Constructor<? extends Directive> constructor = clazz.getConstructor(Configuration.class, OutputStream.class);
				return constructor.newInstance(config, outputStream);
			} catch (ReflectiveOperationException | IllegalArgumentException | SecurityException e) {
				throw new IllegalArgumentException(String.format("Unable to construct directive '%s'", text), e);
			}
		}
		throw new IllegalArgumentException(String.format("Unable to find directive '%s'", text));
	}
}
