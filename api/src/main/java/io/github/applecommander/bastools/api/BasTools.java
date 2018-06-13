package io.github.applecommander.bastools.api;

/**
 * Since there are many pieces to bastools, the version information is just a small, 
 * dedicated class.
 */
public class BasTools {
	public static final String VERSION;
	public static final String TITLE;
	static {
		TITLE = BasTools.class.getPackage().getImplementationTitle();
		VERSION = BasTools.class.getPackage().getImplementationVersion();
	}	
	
	private BasTools() {}
}
