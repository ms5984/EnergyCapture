package com.github.ms5984.energycapture.collectors;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MaterialConversion {
    private static MaterialConversion instance;
    private final Map<String, Integer> materialValues;
    public MaterialConversion(FileConfiguration configuration) {
        instance = this;
        materialValues = new HashMap<>();
        materialValues.put("default", 1);
        final ConfigurationSection section = configuration.getConfigurationSection("material-values");
        if (section == null) return;
        final Map<String, Object> values = section.getValues(false);
        values.forEach((s, o) -> {
            if (o instanceof Integer) {
                materialValues.put(s, (Integer) o);
            }
        });
    }
    public static void fromPlayerEnderchest(Collector collector) {
        final Player player = collector.getPlayerOnline();
        if (player == null) {
            return;
        }
        final Inventory playerEnderChest = player.getEnderChest();
        final ItemStack item = playerEnderChest.getItem(0);
        if (item == null) return;
        final Material material = item.getType();
        final String matName = material.name();
        final int convertQueueCount = instance.materialValues.getOrDefault(matName, instance.materialValues.get("default"));
        if (convertQueueCount >= collector.total) return;
        if (playerEnderChest.addItem(new ItemStack(material, 1)).isEmpty()) {
            collector.total -= convertQueueCount;
        }
/*            if (item.getType() == Material.DIRT) {
                final int convertQueueCount = 1;
                player.getEnderChest().addItem(new ItemStack(Material.DIRT, convertQueueCount));
                collector.total -= convertQueueCount;
            }*/
    }
}
