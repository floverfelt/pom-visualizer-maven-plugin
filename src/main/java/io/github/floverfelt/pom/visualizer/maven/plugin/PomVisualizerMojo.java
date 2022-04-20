package io.github.floverfelt.pom.visualizer.maven.plugin;


import com.google.gson.JsonObject;
import io.github.floverfelt.pom.visualizer.maven.plugin.mermaid.Flowchart;
import io.github.floverfelt.pom.visualizer.maven.plugin.mermaid.MermaidEntity;
import io.github.floverfelt.pom.visualizer.maven.plugin.mermaid.Node;
import io.github.floverfelt.pom.visualizer.maven.plugin.mermaid.Subgraph;
import io.github.floverfelt.pom.visualizer.maven.plugin.mermaid.styles.StyleUtils;
import io.github.floverfelt.pom.visualizer.maven.plugin.utils.PluginWrapper;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "visualize", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
public class PomVisualizerMojo extends AbstractMojo {

  /** Maps modeling the various lifecycle phases */
  private static final String[] LIFECYCLE_IDS = Arrays.stream(LifecyclePhase.values()).map(LifecyclePhase::id).toArray(String[]::new);
  private static final String[] DEFAULT_PHASES = Arrays.copyOfRange(LIFECYCLE_IDS, 0, 23);
  private static final String[] CLEAN_PHASES = Arrays.copyOfRange(LIFECYCLE_IDS, 23, 26);
  private static final String[] SITE_PHASES = Arrays.copyOfRange(LIFECYCLE_IDS, 26, 30);

  @Parameter(defaultValue = "${project}", readonly = true, required = true)
  private MavenProject mavenProject;

  /** Whether to skip the plugin execution or not */
  @Parameter(property= "skip", defaultValue = "false")
  private boolean skip;

  /** Whether to open the visualization in the browser after the plugin executes  */
  @Parameter(property = "openAfterExecution", defaultValue = "true")
  private boolean openAfterExecution;

  /** Whether to skip rendering phases which have no plugins attached to them */
  @Parameter(property = "skipEmptyPhases", defaultValue = "true")
  private boolean skipEmptyPhases;

  public void execute() throws MojoExecutionException, MojoFailureException {

    if (this.skip) {
      getLog().info("pom-visualizer-maven-plugin execution skipped.");
      return;
    }

    // Map the phases of the various lifecycles to their executions
    LinkedHashMap<String, List<PluginWrapper>> pluginDefaultPhaseBindings = new LinkedHashMap<>();
    Arrays.stream(DEFAULT_PHASES).forEach(lifecyclePhase -> pluginDefaultPhaseBindings.put(lifecyclePhase, new ArrayList<>()));

    LinkedHashMap<String, List<PluginWrapper>> pluginCleanPhaseBindings = new LinkedHashMap<>();
    Arrays.stream(CLEAN_PHASES).forEach(lifecyclePhase -> pluginCleanPhaseBindings.put(lifecyclePhase, new ArrayList<>()));

    LinkedHashMap<String, List<PluginWrapper>> pluginSitePhaseBindings = new LinkedHashMap<>();
    Arrays.stream(SITE_PHASES).forEach(lifecyclePhase -> pluginSitePhaseBindings.put(lifecyclePhase, new ArrayList<>()));

    // Parse the build and populate the maps...
    try {
      List<Plugin> buildPlugins = mavenProject.getBuildPlugins();
      if (buildPlugins == null) {
        getLog().warn("No build plugins detected... An empty diagram will be created.");
      } else {
        buildPlugins.forEach(plugin -> {
          getLog().debug(String.format("Build plugin: %s execution(s) %s", plugin.getId(), plugin.getExecutions().size()));
          List<PluginExecution> pluginExecutions = plugin.getExecutions();
          if (pluginExecutions.size() > 0) {
            pluginExecutions.forEach(pluginExecution -> {

              // Store the plugin execution for later grepping
              String executionPhase = pluginExecution.getPhase();

              if (executionPhase == null || executionPhase.equalsIgnoreCase("none")
                      || executionPhase.equals("")) {
                return;
              }
              if (pluginDefaultPhaseBindings.containsKey(executionPhase)) {
                pluginDefaultPhaseBindings.get(executionPhase).add(new PluginWrapper(plugin, pluginExecution));
                return;
              }
              if (pluginCleanPhaseBindings.containsKey(executionPhase)) {
                pluginCleanPhaseBindings.get(executionPhase).add(new PluginWrapper(plugin, pluginExecution));
                return;
              }
              if (pluginSitePhaseBindings.containsKey(executionPhase)) {
                pluginSitePhaseBindings.get(executionPhase).add(new PluginWrapper(plugin, pluginExecution));
              }
            });
          }
        });
      }

      // Build the flowcharts
      Flowchart cleanFlowchart = new Flowchart();
      cleanFlowchart.addItem(buildSubgraph(pluginCleanPhaseBindings, "clean"));

      Flowchart defaultFlowchart = new Flowchart();
      defaultFlowchart.addItem(buildSubgraph(pluginDefaultPhaseBindings, "default"));

      Flowchart siteFlowchart = new Flowchart();
      siteFlowchart.addItem(buildSubgraph(pluginSitePhaseBindings, "site"));

      // Build the json
      JsonObject nodeJson = new JsonObject();
      Arrays.asList(cleanFlowchart, defaultFlowchart, siteFlowchart).forEach(flowchart -> {

        Deque<MermaidEntity> queue = new ArrayDeque<>(flowchart.getSubgraphs());
        while (!queue.isEmpty()) {
          MermaidEntity item = queue.poll();
          if (item instanceof Subgraph) {
            Subgraph subgraph = (Subgraph) item;
            if (subgraph.getSubgraphItemsSize() != 0) {
              queue.addAll(subgraph.getSubgraphItems());
            }
          }
          if (item instanceof Node) {
            Node node = (Node) item;
            String configuration = "";
            PluginExecution nodePluginExecution = node.getPluginWrapper().getPluginExecution();
            String execId = "";

            if (nodePluginExecution != null &&
                    nodePluginExecution.getConfiguration() != null && nodePluginExecution.getConfiguration().toString() != null) {
              configuration = nodePluginExecution.getConfiguration().toString();
            }
            if (nodePluginExecution != null && nodePluginExecution.getId() != null) {
              execId = nodePluginExecution.getId();
            }
            JsonObject nestedJson = new JsonObject();
            nestedJson.addProperty("id", node.getPluginWrapper().getPlugin().getArtifactId());
            nestedJson.addProperty("execId", execId);
            nestedJson.addProperty("config", configuration);
            nodeJson.addProperty(node.getId(), nestedJson.toString());
          }
        }
      });

      // Fetch the html/js/md files
      String html = getResourceFileAsString("index.html");
      String mermaidJs = getResourceFileAsString("mermaid.min.js");
      String md = getResourceFileAsString("visualization.md");

      if (html == null || mermaidJs == null || md == null) {
        String error = "Unable to read pom-visualizer-maven-plugin resource files.";
        getLog().error(error);
        throw new MojoExecutionException(error);
      }

      // Render the flowcharts
      String cleanFlowchartRender = cleanFlowchart.render();
      String defaultFlowchartRender = defaultFlowchart.render();
      String siteFlowchartRender = siteFlowchart.render();

      // Perform the HTML substitution, faux-templating language
      html = html.replace("@artifact-id@", this.mavenProject.getArtifactId());
      html = html.replace("@artifact-version@", this.mavenProject.getVersion());
      html = html.replace("@node_json@", nodeJson.toString());
      html = html.replace("@clean@", cleanFlowchartRender);
      html = html.replace("@default@", defaultFlowchartRender);
      html = html.replace("@site@", siteFlowchartRender);

      // Perform the markdown substitution, faux-templating language
      md = md.replace("@clean@", cleanFlowchartRender);
      md = md.replace("@default@", defaultFlowchartRender);
      md = md.replace("@site@", siteFlowchartRender);

      // Generate paths
      Path outputDir = Path.of(this.mavenProject.getBuild().getDirectory(), "pom-visualizer-maven-plugin");
      Path htmlOutputPath = Paths.get(outputDir.toString(), "visualization.html");
      Path mermaidJsOutputPath = Paths.get(outputDir.toString(), "mermaid.min.js");
      Path mdOutputPath = Paths.get(outputDir.toString(), "visualization.md");

      // Write the output
      Files.createDirectories(outputDir);
      Files.write(htmlOutputPath, Collections.singleton(html));
      Files.write(mermaidJsOutputPath, Collections.singleton(mermaidJs));
      Files.write(mdOutputPath, Collections.singleton(md));

      if (this.openAfterExecution) {
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(htmlOutputPath.toUri());
      }

    } catch (Exception exception) {
      getLog().error("Failed to execute pom-visualizer-maven-plugin.", exception);
      throw new MojoExecutionException(exception);
    }
  }

  /**
   * Method to build a subgraph for the given lifecycle phase - clean, default, site
   *
   * @param phaseBindings mapping of the plugins to the phases
   * @param phaseName the phase name to generate the subgraph for
   * @return Subgraph of the given phase
   */
  private Subgraph buildSubgraph(LinkedHashMap<String, List<PluginWrapper>> phaseBindings, String phaseName) {
    Subgraph phaseSubgraph = new Subgraph(String.format("%sWrapper", phaseName),
            String.format("%s Lifecycle...", phaseName), StyleUtils.WRAPPER_SUBGRAPH_STYLES);
    phaseBindings.forEach((subPhase, pluginWrappers) -> {

      // Skip the subphase if empty
      if (this.skipEmptyPhases && pluginWrappers.isEmpty()) {
        return;
      }

      Subgraph subPhaseSubgraph = new Subgraph(subPhase, subPhase, StyleUtils.PHASE_SUBGRAPH_STYLES);
      // Add nodes to subgraph
      for (int i = 0; i < pluginWrappers.size(); i++) {
        PluginWrapper pluginWrapper = pluginWrappers.get(i);
        String uniqueId = String.format("%s:%s", pluginWrapper.getPlugin().getArtifactId(),
                pluginWrapper.getPluginExecution().getId());
        Node pluginNode = new Node(uniqueId, pluginWrapper);
        // Link the nodes if need be...
        if (i > 0) {
          subPhaseSubgraph.getLastSubgraphEntry().setNext(pluginNode);
        }
        subPhaseSubgraph.addSubgraphItem(pluginNode);
      }
      // Link the sub-graphs if need be
      if (phaseSubgraph.getSubgraphItemsSize() > 0) {
        phaseSubgraph.getLastSubgraphEntry().setNext(subPhaseSubgraph);
      }
      phaseSubgraph.addSubgraphItem(subPhaseSubgraph);
    });
    return phaseSubgraph;
  }

  /**
   * Helper function to read a resource file as a string
   *
   * @param fileName the file name to read
   * @return String of the file
   * @throws IOException if the file cannot be read
   */
  private static String getResourceFileAsString(String fileName) throws IOException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream is = classLoader.getResourceAsStream(fileName)) {
      if (is == null) return null;
      try (InputStreamReader isr = new InputStreamReader(is);
           BufferedReader reader = new BufferedReader(isr)) {
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
      }
    }
  }

}
