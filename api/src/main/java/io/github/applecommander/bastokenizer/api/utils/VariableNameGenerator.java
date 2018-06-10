package io.github.applecommander.bastokenizer.api.utils;

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
