package io.github.applecommander.bastokenizer.tools.bt;

import io.github.applecommander.bastokenizer.api.Optimization;
import picocli.CommandLine.ITypeConverter;

/** Add support for lower-case Optimization flags. */
public class OptimizationTypeConverter implements ITypeConverter<Optimization> {
	@Override
	public Optimization convert(String value) throws Exception {
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
