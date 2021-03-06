package io.github.floverfelt.pom.visualizer.maven.plugin;


import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PomVisualizerMojoTest {

    private static final URL poms = ClassLoader.getSystemResource("poms");

    private static final String goal = "visualize";

    @Rule
    public MojoRule rule = new MojoRule() {
        @Override
        protected void before() { }

        @Override
        protected void after() { }
    };


    @Test
    public void singlePomTest() throws Exception {
        // TODO: Uncomment for local testing, add more robust test cases
        // rule.executeMojo(getTestPom("singlePomTest"), goal);
    }

    @Test
    public void childPomTest() throws Exception {
        // TODO: Uncomment for local testing, add more robust test cases
        // rule.executeMojo(getTestPom("parentPomTest/childPomTest"), goal);
    }

    private static File getTestPom(String testName) throws URISyntaxException {
        Path integrationFolder = Paths.get(Paths.get(poms.toURI()).toString(), testName);
        return new File(String.valueOf(integrationFolder));
    }
}
