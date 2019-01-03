package de.skyshard.plots;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
    /**
     * Fired when the server enables the plugin.
     */
    @Override
    public void onEnable()
    {
        PluginManager pluginManager = getServer().getPluginManager();

        final String pluginName = "WorldGuard";
        Plugin worldGuardPlugin = pluginManager.getPlugin(pluginName);

        if (!(worldGuardPlugin instanceof WorldGuardPlugin)) {
            System.out.println("[Plots] missing plugin dependency: " + pluginName);
            pluginManager.disablePlugin(this);
            return;
        }

        WorldGuardPlugin worldGuard = (WorldGuardPlugin)worldGuardPlugin;
        BlockListener blockListener = new BlockListener(worldGuard);
        pluginManager.registerEvents(blockListener, this);

        System.out.print("[Plots] plugin successfully loaded");
    }

    /**
     * Fired when the server stops and disables all plugins.
     */
    @Override
    public void onDisable()
    {
    }
}
