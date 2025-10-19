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
package io.github.applecommander.bastools.tools.st;

import java.util.ArrayList;
import java.util.List;

import picocli.CommandLine.ITypeConverter;

public class IntegerRangeTypeConverter implements ITypeConverter<List<Integer>> {
    @Override
    public List<Integer> convert(String value) {
        List<Integer> list = new ArrayList<>();
        String[] parts = value.split(",");
        for (String part : parts) {
            String[] range = part.split("-");
            if (range.length == 1) {
                list.add(Integer.parseInt(range[0]));
            } else if (range.length == 2) {
                int i0 = Integer.parseInt(range[0]);
                int i1 = Integer.parseInt(range[1]);
                for (int i = i0; i <= i1; i++) {
                    list.add(i);
                }
            } else {
                throw new RuntimeException("Expecting a single integer or two integers for a range");
            }
        }
        return list;
    }
}
