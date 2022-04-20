package io.github.floverfelt.pom.visualizer.maven.plugin.mermaid;

import io.github.floverfelt.pom.visualizer.maven.plugin.mermaid.styles.StyleUtils;

import java.util.LinkedList;
import java.util.List;

/** A flowchart is composed of sub-graphs */
public class Flowchart {

    private final List<Subgraph> subgraphs = new LinkedList<>();

    public void addItem(Subgraph subgraph) {
        subgraphs.add(subgraph);
    }

    public String render() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("flowchart TB \n");
        for (Subgraph subgraph : subgraphs) {
            sb.append(subgraph.render());
        }
        sb.append("linkStyle default ").append(String.join(",", StyleUtils.DEFAULT_LINK_STYLES)).append("\n");
        return sb.toString();
    }

    public List<Subgraph> getSubgraphs() {
        return this.subgraphs;
    }
}
