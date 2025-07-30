# Graal Native Image configuration

To _update_ the configurations, use:

```declarative
-agentlib:native-image-agent=config-merge-dir=tools/bt/src/main/resources/META-INF/native-image
```

Note that there is a script to exercise a good set options in `scripts/capture-bt.sh`. 
Run it from the project root.
