/*
 * bastools
 * Copyright (C) 2026  Robert Greene
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
package org.applecommander.bastools.api.proofreaders;

import org.applecommander.bastools.api.Configuration;

public class NibbleCheckitApplesoft implements ApplesoftInputBufferProofReader {
    private final Configuration config;
    private final NibbleCheckitChecksum totalChecksum = new NibbleCheckitChecksum();
    private final NibbleCheckitChecksum lineChecksum = new NibbleCheckitChecksum();

    public NibbleCheckitApplesoft(Configuration config) {
        this.config = config;
    }

    @Override
    public Configuration getConfiguration() {
        return config;
    }

    /** {@inheritDoc} */
    @Override
    public void addProgramText(String code) {
        System.out.println("Nibble Checkit, Copyright 1988, Microsparc Inc.");
        ApplesoftInputBufferProofReader.super.addProgramText(code);
        System.out.printf("TOTAL: %04X\n", getTotalChecksumValue());
    }

    /** {@inheritDoc} */
    @Override
    public void addLine(final String originalLine) {
        // Calculate and output line value
        lineChecksum.reset();

        // The ? => PRINT replacement always occurs, including in strings!
        String line = originalLine.replace("?", "PRINT");

        boolean inQuote = false;
        StringBuilder remLettersSeen = new StringBuilder();
        for (char ch : line.toCharArray()) {
            if (ch == '"') inQuote = !inQuote;
            if (!inQuote && ch == ' ') continue;
            lineChecksum.add(ch|0x80);

            // we only allow R E M from a comment; skip rest of the comment
            remLettersSeen.append(ch);
            if ("REM".contentEquals(remLettersSeen)) {
                break;
            }
            else if ("R".contentEquals(remLettersSeen) || "RE".contentEquals(remLettersSeen)) {
                // Keep them, this may be a comment
            }
            else {
                remLettersSeen = new StringBuilder();
            }
        }

        System.out.printf("%02X | %s\n", getLineChecksumValue(), originalLine);

        // Update program values
        int lineNumber = Integer.parseInt(line.split(" ")[0].trim());
        totalChecksum.add(lineNumber & 0xff);
        totalChecksum.add(lineNumber >> 8);
    }

    public int getLineChecksumValue() {
        return ( (lineChecksum.value() & 0xff) - (lineChecksum.value() >> 8) ) & 0xff;
    }
    public int getTotalChecksumValue() {
        int high = totalChecksum.value() & 0xff;
        int low = (totalChecksum.value() >> 8) & 0xff;
        return high << 8 | low;
    }
}
