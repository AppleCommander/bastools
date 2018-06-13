package io.github.applecommander.bastools.tools.bt;

import io.github.applecommander.applesingle.AppleSingle;
import io.github.applecommander.bastools.api.BasTools;
import picocli.CommandLine.IVersionProvider;

/** Display version information.  Note that this is dependent on Gradle configuration. */
public class VersionProvider implements IVersionProvider {
    public String[] getVersion() {
    	return new String[] { 
    		String.format("%s: %s", Main.class.getPackage().getImplementationTitle(), 
    				Main.class.getPackage().getImplementationVersion()),
    		String.format("%s: %s", BasTools.TITLE, BasTools.VERSION),
    		String.format("AppleSingle API: %s", AppleSingle.VERSION)
    	};
    }
}
