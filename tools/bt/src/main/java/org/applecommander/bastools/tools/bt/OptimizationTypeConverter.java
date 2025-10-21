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
package org.applecommander.bastools.tools.bt;

import org.applecommander.bastools.api.Optimization;
import picocli.CommandLine.ITypeConverter;

/** Add support for lower-case Optimization flags. */
public class OptimizationTypeConverter implements ITypeConverter<Optimization> {
	@Override
	public Optimization convert(String value) {
		try {
			return Optimization.valueOf(value);
		} catch (IllegalArgumentException ex) {
			for (Optimization opt : Optimization.values()) {
				String checkName = opt.name().replace('_', '-');
				if (checkName.equalsIgnoreCase(value)) {
					return opt;
				}
			}
			throw ex;
		}
	}
}
