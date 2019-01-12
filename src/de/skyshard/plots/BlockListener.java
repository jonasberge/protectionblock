package de.skyshard.plots;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minecraft.server.v1_12_R1.BlockStainedGlassPane;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.map.MapPalette;
import org.bukkit.material.Colorable;
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
        final Material fenceMaterial = Material.STAINED_GLASS_PANE;
        final byte fenceData = MapPalette.WHITE;
        final String regionPattern = "[^A-Za-z0-9_,'\\-+/]";
        final int height = 60, minHeight = 1, maxHeight = 255;
        // TODO: you're not allowed to set a block outside the bounds.
        // TODO: height may only be greater than 0.
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

        int x = pos.getBlockX(), y = pos.getBlockY(), z = pos.getBlockZ();

        // the cuboid always needs to have the same height, no matter where the block is placed.
        // ideally both the ceiling and the bottom of the region should be evenly distanced from the
        // position at which the block was placed, though this can't happen if it is placed too close
        // to one of the bounds of the to be created region. in that case the offsets need to be
        // shifted in one or the other direction.
        int below = height / 2, above = height - below - 1;
        if (y - below < minHeight) above = height - (below = y - minHeight) - 1;
        if (y + above > maxHeight) below = height - (above = maxHeight - y) - 1;

        // the region is defined as a cuboid, where each side is equally distant from the center block.
        BlockVector pointA = new BlockVector(x - radius, y - below, z - radius);
        BlockVector pointB = new BlockVector(x + radius, y + above, z + radius);
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

        placeFence(new Location(world, x - radius, y, z - radius), Direction.EAST, fenceMaterial, fenceData, 2 * radius);
        placeFence(new Location(world, x + radius, y, z - radius), Direction.SOUTH, fenceMaterial, fenceData, 2 * radius);
        placeFence(new Location(world, x + radius, y, z + radius), Direction.WEST, fenceMaterial, fenceData, 2 * radius);
        placeFence(new Location(world, x - radius, y, z + radius), Direction.NORTH, fenceMaterial, fenceData, 2 * radius);
    }

    private void placeFence(Location loc, Direction dir, Material material, byte data, int length)
    {
        for (int i = 0; i < length; ++i, loc.add(dir.getVector())) {
            Block block = loc.getBlock();
            if (block.getType() == Material.AIR) {
                block.setType(material);
                block.setData((byte)data);
            }
        }
    }
}
