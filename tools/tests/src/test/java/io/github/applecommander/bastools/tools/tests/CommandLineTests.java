package io.github.applecommander.bastools.tools.tests;

import io.github.a2geek.clth.Config;
import io.github.a2geek.clth.JUnitHelper;
import io.github.a2geek.clth.TestHarness;
import io.github.a2geek.clth.TestSuite;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

public class CommandLineTests {
    @ParameterizedTest(name = "{1}: {2}")
    @MethodSource("testCasesBT")
    public void testBT(TestSuite testSuite, String name, String parameters) {
        final TestHarness.Settings settings = TestHarness.settings()
            .deleteFiles()
            .enableAlwaysShowOutput()
            .get();
        TestHarness.run(testSuite, JUnitHelper::execute, settings);
    }

    @ParameterizedTest(name = "{1}: {2}")
    @MethodSource("testCasesST")
    public void testST(TestSuite testSuite, String name, String parameters) {
        final TestHarness.Settings settings = TestHarness.settings()
            .deleteFiles()
            .enableAlwaysShowOutput()
            .get();
        TestHarness.run(testSuite, JUnitHelper::execute, settings);
    }

    public static Stream<Arguments> testCasesBT() {
        try (InputStream inputStream = CommandLineTests.class.getResourceAsStream("/bt-config.yml")) {
            assert inputStream != null;
            String document = new String(inputStream.readAllBytes());
            Config config = Config.load(document);

            return TestSuite.build(config)
                .map(t -> Arguments.of(t, t.testName(), String.join(" ", t.variables().values())));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Stream<Arguments> testCasesST() {
        try (InputStream inputStream = CommandLineTests.class.getResourceAsStream("/st-config.yml")) {
            assert inputStream != null;
            String document = new String(inputStream.readAllBytes());
            Config config = Config.load(document);

            return TestSuite.build(config)
                .map(t -> Arguments.of(t, t.testName(), String.join(" ", t.variables().values())));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
