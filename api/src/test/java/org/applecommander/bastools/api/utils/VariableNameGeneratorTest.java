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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class VariableNameGeneratorTest {
	@Test
	public void testNameSequence() {
		Map<Integer,String> expecteds = new HashMap<>();
		expecteds.put(0, "A");
		expecteds.put(25, "Z");
		expecteds.put(26, "AA");
		expecteds.put(51, "ZA");
		expecteds.put(52, "AB");
		expecteds.put(77, "ZB");
		// very last name in sequence
		expecteds.put(VariableNameGenerator.LENGTH-1, "Z9");
		
		int lastCheck = expecteds.keySet().stream().max(Integer::compare).get();
		
		VariableNameGenerator gen = new VariableNameGenerator();
		for (int i = 0; i <= lastCheck; i++) {
			String varName = gen.get().orElseThrow(() -> new RuntimeException("Ran out of variable names too early!"));
			if (expecteds.containsKey(i)) {
				assertEquals(expecteds.get(i), varName);
			}
		}
	}
	
	@Test
	public void testSequenceLength() {
		VariableNameGenerator gen = new VariableNameGenerator();
		int count = 0;
		while (gen.get().isPresent()) count++;
		assertEquals(VariableNameGenerator.LENGTH, count);
	}
}
