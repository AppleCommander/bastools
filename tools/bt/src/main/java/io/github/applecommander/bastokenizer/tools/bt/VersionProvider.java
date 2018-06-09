package io.github.applecommander.bastokenizer.tools.bt;

import io.github.applecommander.applesingle.AppleSingle;
import io.github.applecommander.bastokenizer.api.BasTokenizer;
import picocli.CommandLine.IVersionProvider;

/** Display version information.  Note that this is dependent on Gradle configuration. */
public class VersionProvider implements IVersionProvider {
    public String[] getVersion() {
    	return new String[] { 
    		String.format("BT CLI: %s", Main.class.getPackage().getImplementationVersion()),
    		String.format("BT API: %s", BasTokenizer.VERSION),
    		String.format("AppleSingle API: %s", AppleSingle.VERSION)
    	};
    }
}
