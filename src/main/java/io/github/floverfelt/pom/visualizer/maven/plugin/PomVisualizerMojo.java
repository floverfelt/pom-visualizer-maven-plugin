package io.github.floverfelt.pom.visualizer.maven.plugin;


import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.Arrays;
import java.util.List;

@Mojo(name = "visualize", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
public class PomVisualizerMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject mavenProject;


/*
  @Component
  private BuildPluginManager pluginManager;*/

  public void execute() throws MojoExecutionException, MojoFailureException {


    List<Plugin> buildPlugins = mavenProject.getBuildPlugins();
    //Arrays.stream(LifecyclePhase.values()).forEach(System.out::println);
    if (buildPlugins == null) {
      getLog().info("No build plugins detected.");
    } else {
      buildPlugins.forEach(plugin -> {
        System.out.println(String.format("Plugin: %s executions %s", plugin.getId(), plugin.getExecutions().size()));
      });
    }

  }

}
