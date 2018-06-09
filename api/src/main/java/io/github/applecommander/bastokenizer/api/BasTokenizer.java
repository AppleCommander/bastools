package io.github.applecommander.bastokenizer.api;

/**
 * Since there are many pieces to bastokenizer, the version information is just a small, 
 * dedicated class.
 */
public class BasTokenizer {
	public static final String VERSION;
	static {
		VERSION = BasTokenizer.class.getPackage().getImplementationVersion();
	}	
	
	private BasTokenizer() {}
}
