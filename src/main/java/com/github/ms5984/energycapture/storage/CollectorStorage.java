package com.github.ms5984.energycapture.storage;

import com.github.ms5984.energycapture.EnergyCapture;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import com.github.ms5984.energycapture.collectors.Collector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CollectorStorage {
    private static File file = new File(EnergyCapture.getInstance().getDataFolder(), "collectors.yml");
    private FileConfiguration storage;
    private static CollectorStorage instance;

    private CollectorStorage() {
        initializeStorage();
        loadInCollectors();
    }

    private void initializeStorage() {
        if (!file.exists()) {
            storage = new YamlConfiguration();
            storage.createSection("collectors");
            try {
                storage.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            storage = YamlConfiguration.loadConfiguration(file);
        }
    }
    private void loadInCollectors() {
        @SuppressWarnings("unchecked")
        List<Collector> collectors = (List<Collector>) storage.getList("collectors");
        if (collectors != null) {
            collectors.forEach(Collector::registerFromConfig);
            return;
        }
        System.out.println("No collectors loaded");
    }

    public static void initialize() {
        if (instance != null) {
            return;
        }
        instance = new CollectorStorage();
    }
    public static void saveFile() {
        try {
            instance.storage.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void saveCollector(Collector collector) {
        if (!file.exists()) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<Collector> configCollectors = (List<Collector>) instance.storage.getList("collectors");
        if (configCollectors == null) {
            configCollectors = new ArrayList<>();
        }
        configCollectors.add(collector);
        instance.storage.set("collectors", configCollectors);
        if (EnergyCapture.getInstance().isEnabled()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    saveFile();
                }
            }.runTaskAsynchronously(EnergyCapture.getInstance());
        }
    }
    public static void removeCollector(UUID uuid) {
        @SuppressWarnings("unchecked")
        List<Collector> configCollectors = (List<Collector>) instance.storage.getList("collectors");
        if (configCollectors == null) {
            return;
        }
        configCollectors.removeIf(collector -> (collector.getUUID().equals(uuid)));
        instance.storage.set("collectors", configCollectors);
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    instance.storage.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTask(EnergyCapture.getInstance());
    }
}
