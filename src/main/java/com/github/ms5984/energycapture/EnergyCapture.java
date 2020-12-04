package com.github.ms5984.energycapture;

import com.github.ms5984.energycapture.collectors.MaterialConversion;
import com.github.ms5984.energycapture.listeners.BlockListeners;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.ms5984.energycapture.collectors.Collector;
import com.github.ms5984.energycapture.listeners.SignListener;
import com.github.ms5984.energycapture.storage.CollectorStorage;

//import java.util.logging.Logger;

public final class EnergyCapture extends JavaPlugin {
    private static EnergyCapture instance;
    private FileConfiguration configuration;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        ConfigurationSerialization.registerClass(Collector.class);
        saveDefaultConfig();
        reloadConfig();
        configuration = getConfig();
        loadInConfig();
        getServer().getPluginManager().registerEvents(new SignListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListeners(), this);
        CollectorStorage.initialize();
        Collector.setupUpdateTask();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Collector.saveAllCollectors();
        instance = null;
    }

    public void loadInConfig() {
        Collector.setMaterial((String) configuration.get("collectors.material"));
//        Collector.setRateDivisor((String) configuration.get("collectors.collection-rate-divisor"));
        Collector.setBaseConversionRate(configuration.getDouble("collectors.base-rate"));
        Collector.setLowDropOutThreshold(configuration.getInt("collectors.low-drop-out-threshold"));
        new MaterialConversion(configuration);
    }
    public static EnergyCapture getInstance() {
        return instance;
    }
/*    public static Logger getLog() {
        return instance.getLogger();
    }*/
}
