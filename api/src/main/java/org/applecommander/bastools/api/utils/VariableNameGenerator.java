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
package org.applecommander.bastools.api.utils;

import java.util.Optional;
import java.util.function.Supplier;

/** Generate all Applesoft BASIC FP variable names. */
public class VariableNameGenerator implements Supplier<Optional<String>> {
	public static final String CHAR1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String CHAR2 = " " + CHAR1 + "0123456789";
	public static final int LENGTH = CHAR1.length() * CHAR2.length();

	private int n = 0;
	
	@Override
	public Optional<String> get() {
		try {
			if (n >= 0 && n < LENGTH) {
				return Optional.of(String.format("%s%s", 
										CHAR1.charAt(n % CHAR1.length()), 
										CHAR2.charAt(n / CHAR1.length())
									).trim());
			}
			return Optional.empty();
		} finally {
			n += 1;
		}
	}
}
