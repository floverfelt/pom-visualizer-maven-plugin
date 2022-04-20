package io.github.floverfelt.pom.visualizer.maven.plugin.utils;

import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;

/** Wrapper for tying a Maven plugin to its execution context */
public class PluginWrapper {

    private final PluginExecution pluginExecution;

    private final Plugin plugin;

    public PluginWrapper(Plugin plugin, PluginExecution pluginExecution) {
        this.plugin = plugin;
        this.pluginExecution = pluginExecution;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public PluginExecution getPluginExecution() {
        return pluginExecution;
    }

}
