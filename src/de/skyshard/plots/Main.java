package de.skyshard.plots;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
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
        WorldGuardPlugin worldGuard = (WorldGuardPlugin)pluginManager.getPlugin("WorldGuard");

        BlockListener blockListener = new BlockListener(worldGuard);
        pluginManager.registerEvents(blockListener, this);
    }

    /**
     * Fired when the server stops and disables all plugins.
     */
    @Override
    public void onDisable()
    {
    }
}
