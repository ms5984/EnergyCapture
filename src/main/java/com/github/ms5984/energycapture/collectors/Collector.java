package com.github.ms5984.energycapture.collectors;

import com.github.ms5984.energycapture.EnergyCapture;
import com.github.ms5984.energycapture.storage.CollectorStorage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Collector implements ConfigurationSerializable {
    protected int total = 0;
    private double buffer = 0;
    private OfflinePlayer player;
    private UUID uuid;
    protected Block modifierBlock;
    protected Block baseBlock;
    protected Block signBlock;
    private BlockFace faceOfSign;
    private Inventory inventory;
    private boolean saving = false;
    private static final Map<UUID, Collector> collectors = new HashMap<>();
    private static final EnergyCapture plugin = EnergyCapture.getInstance();
    private static BukkitTask updateQueue;
    private static Material material;
    private static double baseConversionRate = 1;
    private static int lowDropOutThreshold = 3;

    protected Collector(SignChangeEvent e, Block collectorChest, BlockFace signOnFace) {
        this(e.getPlayer(), collectorChest, signOnFace);
        e.setLine(0, "Not Producing: 0");
        e.setLine(1, ChatColor.translateAlternateColorCodes('&',
                "&3[Collector]"));
        e.setLine(2, ChatColor.translateAlternateColorCodes('&',
                "&aRegistered to "));
        e.setLine(3, ChatColor.translateAlternateColorCodes('&',
                "&6" + e.getPlayer().getDisplayName()));
    }
    private Collector(OfflinePlayer player, Block collectorChest, BlockFace signOnFace) {
        this.player = player;
        this.baseBlock = collectorChest;
        this.signBlock = collectorChest.getRelative(signOnFace);
        this.modifierBlock = collectorChest.getRelative(BlockFace.UP);
        this.uuid = UUID.randomUUID();
        this.faceOfSign = signOnFace;
    }

    protected void update() {
        if (!(signBlock.getType().name().contains("_WALL_SIGN"))) {
            deregister();
            return;
        }
        if (saving) {
            return;
        }
        final Sign sign = ((Sign) signBlock.getState());
        if (!player.isOnline()) {
            sign.setLine(0, ChatColor.translateAlternateColorCodes('&',
                    String.format("Offline: %s", total)));
            return;
        }
        final int lightFromSky = baseBlock.getRelative(BlockFace.UP, 1).getLightFromSky();
        final int lightLevel = baseBlock.getRelative(BlockFace.UP, 1).getLightLevel();
        if (lightFromSky < lightLevel || lightFromSky < lowDropOutThreshold) {
            sign.setLine(0, ChatColor.translateAlternateColorCodes('&',
                    String.format("&eInactive: &r%s", total)));
            sign.update();
            return;
        }
        buffer += baseConversionRate * ((((double) (lightFromSky)) / 15d *
                (determineModifierBuff())));
        total += (int) buffer;
        buffer -= (int) buffer;
        if (lightFromSky >= lowDropOutThreshold) {
            sign.setLine(0, ChatColor.translateAlternateColorCodes('&',
                    String.format("&aProducing: %s", total)));
        }
        sign.update();
        MaterialConversion.fromPlayerEnderchest(this);
    }
    private void deregister() {
        try {
            Objects.requireNonNull(player.getPlayer())
                    .sendMessage(ChatColor.translateAlternateColorCodes('&',
                    String.format("&oDe-registered Collector at x:%s y:%s z:%s!",
                            baseBlock.getX(), baseBlock.getY(), baseBlock.getZ())));
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
//            collectorList.remove(this);
            collectors.remove(this.uuid);
        }
        CollectorStorage.removeCollector(this.uuid);
    }
    public UUID getUUID() {
        return uuid;
    }
    private double determineModifierBuff() {
        switch (modifierBlock.getType()) {
            case LIGHT_WEIGHTED_PRESSURE_PLATE:
                return 5d;
            case HEAVY_WEIGHTED_PRESSURE_PLATE:
                return 4d;
            case STONE_PRESSURE_PLATE:
                return 3d;
            case OAK_PRESSURE_PLATE:
            case BIRCH_PRESSURE_PLATE:
            case SPRUCE_PRESSURE_PLATE:
            case ACACIA_PRESSURE_PLATE:
            case JUNGLE_PRESSURE_PLATE:
            case DARK_OAK_PRESSURE_PLATE:
                return 2d;
            default:
                return 1d;
        }
    }
    public Player getPlayerOnline() {
        return (player.isOnline()) ? player.getPlayer() : null;
    }

    public static void registerNew(SignChangeEvent e, Block attachedBlock, BlockFace signOnFace) {
        final Collector collector = new Collector(e, attachedBlock, signOnFace);
//        collectorList.add(new Collector(e, attachedBlock, signOnFace));
//        collectorList.add(collector);
        collectors.put(collector.uuid, collector);
        e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&',
                String.format("&aRegistered Collector at x:%s y:%s z:%s",
                        attachedBlock.getX(), attachedBlock.getY(), attachedBlock.getZ())));
        CollectorStorage.saveCollector(collector);
    }
    public static void registerFromConfig(Collector collector) {
        if (collector == null) {
            System.out.println("Could not load collector from storage file!");
            return;
        }
//        collectorList.add(collector);
        collectors.put(collector.uuid, collector);
    }
    public static void setupUpdateTask() {
        if (updateQueue != null) {
            updateQueue.cancel();
        }
        updateQueue = new BukkitRunnable() {
            @Override
            public void run() {
//                for (Collector collector : new ArrayList<>(collectorList)) {
                for (Collector collector : new ArrayList<>(collectors.values())) {
                    collector.update();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    public static boolean isCollector(Block testBlock) {
        return collectors.values().stream().anyMatch(collector -> {
            final double x, y, z;
            x = collector.baseBlock.getX();
            y = collector.baseBlock.getY();
            z = collector.baseBlock.getZ();
            return new Location(collector.baseBlock.getWorld(), x, y, z).equals(testBlock.getLocation());
        });
    }
    public static void setMaterial(String configMaterial) {
        if (configMaterial == null) {
            System.out.println("Collector: Couldn't read material from config!");
            return;
        }
        final Material newMaterial = Material.getMaterial(configMaterial.toUpperCase());
        if (newMaterial == null) {
            System.out.println("Collector: Invalid material in config!");
            return;
        }
        material = newMaterial;
    }
    public static Material getMaterial() {
        return material;
    }
    public static void setBaseConversionRate(double rate) { // default above
        baseConversionRate = rate;
    }
    public static void setLowDropOutThreshold(int threshold) {
        lowDropOutThreshold = threshold;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> outer_map = new HashMap<>();
        Map<String, Object> inner_map = new LinkedHashMap<>();
        inner_map.put("uuid", uuid.toString());
        inner_map.put("player", player.getUniqueId().toString());
        Map<String, Object> baseBlockLocation = new LinkedHashMap<>();
        baseBlockLocation.put("world", baseBlock.getWorld().getName());
        baseBlockLocation.put("x", baseBlock.getX());
        baseBlockLocation.put("y", baseBlock.getY());
        baseBlockLocation.put("z", baseBlock.getZ());
        inner_map.put("base-block", baseBlockLocation);
        inner_map.put("sign-on-face", faceOfSign.name());
        outer_map.put("collector", inner_map);
        return outer_map;
    }
    public static Collector deserialize(Map<String, Object> configMap) {
        @SuppressWarnings("unchecked")
        Map<String, Object> innerMap = (Map<String, Object>) configMap.get("collector");
        final UUID uuid = UUID.fromString((String) innerMap.get("uuid"));
        final OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(UUID
                .fromString((String) innerMap.get("player")));
        @SuppressWarnings("unchecked")
        Map<String, Object> baseBlockMap = (Map<String, Object>) innerMap.get("base-block");
        final Block baseBlock = Objects.requireNonNull(plugin.getServer()
                .getWorld((String) baseBlockMap.get("world"))).getBlockAt((int) baseBlockMap.get("x"),
                (int) baseBlockMap.get("y"), (int) baseBlockMap.get("z"));
        final BlockFace blockFace = BlockFace.valueOf((String) innerMap.get("sign-on-face"));
        return new Collector(offlinePlayer, baseBlock, blockFace);
    }
/*    public static List<Collector> getCollectorList() {
        return new ArrayList<>(collectors.values());
    }*/
    public static Collector getCollectorByUuid(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return collectors.get(uuid);
    }
/*    public static Map<UUID, BlockFace> getSignFaceByUUID() {
        final HashMap<UUID, BlockFace> fMap = new HashMap<>();
        for (Collector collector : collectors.values()) {
            fMap.put(collector.uuid, collector.faceOfSign);
        }
        return fMap;
    }*/
    public static UUID getUUIDByBlockBlockFace(Block collectorBlock, BlockFace blockFace) {
        for (Map.Entry<UUID, Collector> entry: collectors.entrySet()) {
            final double x, y, z;
            x = entry.getValue().baseBlock.getX();
            y = entry.getValue().baseBlock.getY();
            z = entry.getValue().baseBlock.getZ();
            if (new Location(entry.getValue().baseBlock.getWorld(), x, y, z)
                    .equals(collectorBlock.getLocation())) {
                return entry.getKey();
            }
        }
        return null;
    }
    public static void saveAllCollectors() {
        System.out.println("Saving collectors...");
        AtomicInteger i = new AtomicInteger();
        collectors.values().forEach(collector -> {
            collector.saving = true;
            CollectorStorage.saveCollector(collector);
            i.incrementAndGet();
        });
        CollectorStorage.saveFile();
        System.out.println(i + " collector" + ((i.get() == 1) ? "" : "s") + " saved.");
    }
}
