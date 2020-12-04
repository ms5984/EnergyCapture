package com.github.ms5984.energycapture.listeners;

import com.github.ms5984.energycapture.collectors.Collector;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockListeners implements Listener {
    private static final List<BlockFace> FACINGS = new ArrayList<>();
    static {
        FACINGS.add(BlockFace.EAST);
        FACINGS.add(BlockFace.NORTH);
        FACINGS.add(BlockFace.SOUTH);
        FACINGS.add(BlockFace.WEST);
    }

    // Make methods preventing breakage of signs+blocks
    @EventHandler(ignoreCancelled = true)
    public void onCollectorBlockBreak(BlockBreakEvent e) {
        Block collectorBlock = e.getBlock();
//        if (block.getType() == Material.AIR) { // update for !(Collector block type)
        if (collectorBlock.getType() != Collector.getMaterial()) { // update for !(Collector block type)
            return;
        }
        if (!signOnBlockFace(collectorBlock)) {
            return;
        }
        if (Collector.isCollector(collectorBlock)) {
            e.setCancelled(true);
        }
    }

    private boolean signOnBlockFace(Block b) {
//        return (b.getRelative(face).getType().name().contains("_WALL_SIGN"));
        boolean isFace = false;
        for (BlockFace blockFace : FACINGS) {
            if ((b.getRelative(blockFace).getBlockData() instanceof WallSign)) {
                isFace = true;
            }
        }
        return isFace;
    }
}
