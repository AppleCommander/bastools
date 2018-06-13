package io.github.applecommander.bastools.tools.bt;

import io.github.applecommander.bastools.api.utils.Converters;
import picocli.CommandLine.ITypeConverter;

/** Add support for "$801" and "0x801" instead of just decimal like 2049. */
public class IntegerTypeConverter implements ITypeConverter<Integer> {
	@Override
	public Integer convert(String value) {
		return Converters.toInteger(value);
	}
}
