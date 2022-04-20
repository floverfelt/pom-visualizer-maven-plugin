package io.github.floverfelt.pom.visualizer.maven.plugin.mermaid;

import java.util.LinkedList;
import java.util.List;

/** A subgraph is composed of a list of nodes or other subgraphs */
public class Subgraph extends MermaidEntity {

    private final LinkedList<MermaidEntity> subgraphItems = new LinkedList<>();

    public void addSubgraphItem(MermaidEntity entity) {
        this.subgraphItems.add(entity);
    }

    public MermaidEntity getLastSubgraphEntry() {
        return this.subgraphItems.getLast();
    }

    public int getSubgraphItemsSize() {
        return this.subgraphItems.size();
    }

    public LinkedList<MermaidEntity> getSubgraphItems() {
        return this.subgraphItems;
    }

    public Subgraph(String id, String label) {
        super(id, label);
    }

    public Subgraph(String id, String label, List<String> styles) {
        super(id, label, styles);
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("subgraph %s[\"%s\"]\n direction TB \n", this.getId(), this.getLabel()));

        for (MermaidEntity entity : subgraphItems) {
            sb.append(entity.render());
        }

        sb.append("end \n");

        if (this.getNext() != null) {
            sb.append(this.getId()).append(this.getLinkType()).append(this.getNext().getId()).append("\n");
        }

        sb.append(this.renderStyles());

        return sb.toString();
    }

}
