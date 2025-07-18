# Graal Native Image configuration

This is a mish-mash of manual and automatic code generation.

To _update_ the configurations, use:

```declarative
-agentlib:native-image-agent=config-merge-dir=tools/st/src/main/resources/META-INF/native-image
```

Please delete empty files and reformat the JSON!

When running the JAR from the command line. This particular pathing, suggests that it be run from the root of the project.

Note: With `st` every subcommand should be executed to capture all the pieces!

For example:

```shell
TODO
```