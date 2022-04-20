package io.github.floverfelt.pom.visualizer.maven.plugin.mermaid;

import java.util.ArrayList;
import java.util.List;

/** POJO representation of a mermaid object - a node or subgraph */
public abstract class MermaidEntity {

    /** The id of the mermaid object */
    private String id;

    /** The label to display to the user */
    private String label = "";

    /** If this mermaid entity is linked to another entity */
    private MermaidEntity next = null;

    /** Styles to apply to the mermaid entity */
    private List<String> styles = new ArrayList<>();

    public MermaidEntity(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public MermaidEntity(String id, String label, List<String> styles) {
        this(id, label);
        this.styles = styles;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public MermaidEntity getNext() {
        return next;
    }

    public void setNext(MermaidEntity next) {
        this.next = next;
    }

    /** The link type, straight line or pointed */
    public String getLinkType() {
        return "-->";
    }

    /** Method to render the entity */
    public abstract String render();

    /** Method to render the styles */
    public String renderStyles() {
        StringBuilder sb = new StringBuilder();
        if (this.styles.size() > 0) {
            sb.append("style ").append(this.id).append(" ");
            sb.append(String.join(",", this.styles));
            sb.append("\n");
        }
        return sb.toString();
    }
}
