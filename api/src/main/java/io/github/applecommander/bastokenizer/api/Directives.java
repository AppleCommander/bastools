package io.github.applecommander.bastokenizer.api;

import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.TreeMap;

import io.github.applecommander.bastokenizer.api.directives.EmbeddedBinaryDirective;
import io.github.applecommander.bastokenizer.api.directives.HexDirective;

public class Directives {
	private Directives() { /* Prevent construction. */ }
	
	private static Map<String,Class<? extends Directive>> DIRECTIVES = 
		new TreeMap<String,Class<? extends Directive>>(String.CASE_INSENSITIVE_ORDER) {
			private static final long serialVersionUID = -8111460701487331592L;

			{
				put("$embed", EmbeddedBinaryDirective.class);
				put("$hex", HexDirective.class);
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
