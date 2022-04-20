package io.github.floverfelt.pom.visualizer.maven.plugin.mermaid.styles;

import java.util.Arrays;
import java.util.List;

/** Util class for holding mermaid js styles */
public class StyleUtils {

    public static final List<String> WRAPPER_SUBGRAPH_STYLES = Arrays.asList("fill:white","stroke:black","color:black");

    public static final List<String> PHASE_SUBGRAPH_STYLES = Arrays.asList("fill:white","stroke:red","color:#d14");

    public static final List<String> DEFAULT_NODE_STYLES = Arrays.asList("fill:white","stroke:blue","color:black");

    public static final List<String> DEFAULT_LINK_STYLES = Arrays.asList("fill:none","stroke:#333333");

}
