package io.github.floverfelt.pom.visualizer.maven.plugin.mermaid;

import io.github.floverfelt.pom.visualizer.maven.plugin.mermaid.styles.StyleUtils;
import io.github.floverfelt.pom.visualizer.maven.plugin.utils.PluginWrapper;

/** POJO representing a node in a subgraph */
public class Node extends MermaidEntity {

    private static int nodeCounter = 0;

    /** The plugin & execution associated with the node */
    private final PluginWrapper pluginWrapper;

    public Node(String label, PluginWrapper pluginWrapper) {
        super(String.format("node%s", nodeCounter), label, StyleUtils.DEFAULT_NODE_STYLES);
        this.pluginWrapper = pluginWrapper;
        Node.nodeCounter += 1;
    }

    public PluginWrapper getPluginWrapper() {
        return this.pluginWrapper;
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getId()).append("{{\"")
                .append(this.getLabel())
                .append("\"}}").append("\n");
        if (this.getNext() != null) {
            sb.append(this.getId()).append(this.getLinkType()).append(this.getNext().getId()).append("\n");
        }

        // Render styles
        sb.append(this.renderStyles());
        return sb.toString();
    }

}
