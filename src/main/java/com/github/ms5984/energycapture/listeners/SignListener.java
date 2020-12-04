package com.github.ms5984.energycapture.listeners;

import com.github.ms5984.energycapture.collectors.Collector;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCollectorSignInitialize(SignChangeEvent e) {
        final String testForCollector = e.getLine(0);
        if (testForCollector == null) {
            return;
        }
        if (ChatColor.stripColor(testForCollector).equalsIgnoreCase("[Collector]")) {
            if (!e.getPlayer().hasPermission("energycapture.collector.place")) {
                return;
            }
            Block sign_block = e.getBlock();
            if (!(sign_block.getBlockData() instanceof WallSign)) {
                return;
            }
            WallSign sign = (WallSign) sign_block.getState().getBlockData();
            Block attachedBlock = sign_block.getRelative(sign.getFacing().getOppositeFace());
//            e.getPlayer().sendMessage(attachedBlock.getType().toString());
//            if (attachedBlock.getType() != Material.ENDER_CHEST) {
            if (attachedBlock.getType() != Collector.getMaterial()) {
                return;
            }
            Collector.registerNew(e, attachedBlock, sign.getFacing());
        }
    }

    // Make methods preventing breakage of signs+blocks
    @EventHandler
    public void onCollectorSignBreak(BlockBreakEvent e) {
        final Block signBlock = e.getBlock();
        if (!(signBlock.getBlockData() instanceof WallSign)) {
            return;
        }
        Sign sign = (Sign) signBlock.getState();
        final String collectorLine = ChatColor.stripColor(sign.getLine(1));
        if (!collectorLine.equals("[Collector]")) {
            return;
        }
        WallSign wallSign = (WallSign) signBlock.getState().getBlockData();
        Block attachedBlock = signBlock.getRelative(wallSign.getFacing().getOppositeFace());
        if (!Collector.isCollector(attachedBlock)) {
            return;
        }
        Collector collector = Collector.getCollectorByUuid(Collector
                .getUUIDByBlockBlockFace(attachedBlock, wallSign.getFacing()));
        if (collector == null) {
            return;
        }
        if (e.getPlayer().equals(collector.getPlayerOnline()) || e.getPlayer()
                .hasPermission("energycapture.collector.adminbreak")) {
            return;
        }
        e.setCancelled(true);
    }
}
