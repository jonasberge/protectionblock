package de.skyshard.plots;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

import java.util.UUID;

public class BlockListener implements Listener
{
    private WorldGuardPlugin worldGuard;

    BlockListener(WorldGuardPlugin worldGuard)
    {
        this.worldGuard = worldGuard;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        // constants for testing purposes.
        final Material centerMaterial = Material.GOLD_BLOCK;
        final Material fenceMaterial = Material.FENCE;
        final String regionPattern = "[^A-Za-z0-9_,'\\-+/]";
        final int maxHeight = 255;
        final int radius = 10;

        Block centerBlock = event.getBlock();
        if (centerBlock.getType() != centerMaterial)
            return;

        World world = centerBlock.getWorld();
        Vector pos = centerBlock.getLocation().toVector();
        UUID uuid = event.getPlayer().getUniqueId();
        String worldName = world.getName().replaceAll(regionPattern, "_");

        // the region name consists of a prefix that indicates that it belongs
        // to this plugin, the sanitized name of the contained world, the uuid
        // of the owning user and the x-, y- and z-components of the center block.
        String regionName = String.format("plots-%s-%s-%d-%d-%d",
                worldName, uuid, pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());

        // the region is defined as a cuboid, where each side is equally distant from the center block.
        BlockVector pointA = new BlockVector(pos.getBlockX() - radius, 0, pos.getBlockZ() - radius);
        BlockVector pointB = new BlockVector(pos.getBlockX() + radius, maxHeight, pos.getBlockZ() + radius);
        ProtectedRegion region = new ProtectedCuboidRegion(regionName, pointA, pointB);

        RegionContainer container = worldGuard.getRegionContainer();
        RegionManager regions = container.get(world);
        if (regions == null) {
            Bukkit.broadcastMessage("[Plots] WorldGuard error: " +
                    "could not load regions of world \"" + world.getName() + "\".");
            return;
        }

        region.getOwners().addPlayer(uuid);
        regions.addRegion(region);

        Location loc = centerBlock.getLocation();
        placeFence(world, loc.getBlockX() - radius, loc.getBlockZ() - radius, 1, 0, 2 * radius, fenceMaterial);
        placeFence(world, loc.getBlockX() + radius, loc.getBlockZ() - radius, 0, 1, 2 * radius, fenceMaterial);
        placeFence(world, loc.getBlockX() + radius, loc.getBlockZ() + radius, -1, 0, 2 * radius, fenceMaterial);
        placeFence(world, loc.getBlockX() - radius, loc.getBlockZ() + radius, 0, -1, 2 * radius, fenceMaterial);
    }

    private void placeFence(World world, int x, int z, int xOffset, int zOffset, int length, Material material)
    {
        for (int i = 0; i < length; ++i, x += xOffset, z += zOffset) {
            Block block = null;
            for (int y = (1 << 8) - 1; y > 0; --y) {
                Block b = world.getBlockAt(x, y, z);
                if (b.getType() != Material.AIR)
                    break;
                block = b;
            }

            if (block != null)
                block.setType(material);
        }
    }
}
