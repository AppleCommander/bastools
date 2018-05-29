package io.github.applecommander.bastokenizer.tools.bt;

import picocli.CommandLine.IVersionProvider;

/** Display version information.  Note that this is dependent on Gradle configuration. */
public class VersionProvider implements IVersionProvider {
    public String[] getVersion() {
    	return new String[] { Main.class.getPackage().getImplementationVersion() };
    }
}
