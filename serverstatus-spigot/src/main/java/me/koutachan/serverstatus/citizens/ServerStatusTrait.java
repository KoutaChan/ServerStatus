package me.koutachan.serverstatus.citizens;

import me.koutachan.serverstatus.ServerStatusSpigot;
import me.koutachan.serverstatus.ServerStatusInfo;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.ArmorStandTrait;
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@TraitName("server-status")
public class ServerStatusTrait extends Trait {
    private String serverId;
    private NPC displayNpc;
    private int displayedLineCount;
    private double lineHeight = Double.NaN;
    private boolean active;

    public ServerStatusTrait() {
        super("server-status");
    }

    public ServerStatusTrait(String serverId) {
        this();
        this.serverId = serverId;
    }

    @Override
    public void run() {
        if (!active) return;

        ServerStatusInfo status = ServerStatusSpigot.INSTANCE.getStatusInfo(serverId);
        if (status != null) {
            if (status.isOnline()) {
                refreshHologram(getConfig("player-count-with-max", "&a%players%/%max-players%"),
                        text -> text.replaceAll("%players%", String.valueOf(status.getOnlinePlayers()))
                                .replaceAll("%max-players%", String.valueOf(status.getMaxPlayers()))
                                .replaceAll("%server-name%", serverId));
            } else {
                refreshHologram(getConfig("server-offline", "&cサーバーはオフラインです"),
                        text -> text.replaceAll("%server-name%", serverId));
            }
        } else {
            refreshHologram(getConfig("unknown-server", "&c情報を取得できませんでした"),
                    text -> text.replaceAll("%server-name%", serverId));
        }
    }

    public Object getConfig(String key, Object defaultValue) {
        FileConfiguration config = ServerStatusSpigot.INSTANCE.getConfig();
        ConfigurationSection section = config.getConfigurationSection(serverId);
        if (section != null) {
            Object value = section.get(key);
            if (value != null) return value;
        }
        return config.get(key, defaultValue);
    }

    @SuppressWarnings("unchecked")
    private void refreshHologram(Object content, Function<String, String> formatter) {
        List<String> lines = new ArrayList<>();
        String[] texts;
        if (content instanceof List) {
            texts = ((List<String>) content).toArray(new String[0]);
        } else {
            texts = content.toString().split("\n");
        }

        for (int i = texts.length - 1; i >= 0; i--) {
            lines.add(formatter.apply(ChatColor.translateAlternateColorCodes('&', texts[i])));
        }

        refreshHologram(lines);
    }

    private void refreshHologram(List<String> lines) {
        HologramTrait trait = getDisplayHologramTrait();
        if (trait == null) return;

        for (int i = 0; i < lines.size(); i++) {
            trait.setLine(i, lines.get(i));
        }

        for (int i = displayedLineCount - 1; i >= lines.size(); i--) {
            trait.removeLine(i);
        }

        displayedLineCount = lines.size();
    }

    private HologramTrait getDisplayHologramTrait() {
        if (!npc.isSpawned()) return null;

        Location location = getDisplayLocation();
        if (displayNpc == null) {
            displayNpc = ServerStatusSpigot.INSTANCE.getStatusDisplayRegistry()
                    .createNPC(EntityType.ARMOR_STAND, "serverstatus-display");
            displayNpc.data().set(NPC.Metadata.SHOULD_SAVE, false);
            displayNpc.setProtected(true);

            ArmorStandTrait armorStandTrait = displayNpc.getOrAddTrait(ArmorStandTrait.class);
            armorStandTrait.setAsHelperEntity(npc);
            displayNpc.getOrAddTrait(HologramTrait.class);
        }

        if (displayNpc.isSpawned()) {
            displayNpc.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        } else if (!displayNpc.spawn(location)) {
            return null;
        }

        HologramTrait trait = displayNpc.getOrAddTrait(HologramTrait.class);
        double configuredLineHeight = ServerStatusSpigot.INSTANCE.getConfig().getDouble("display-height", 0.3);
        if (Double.compare(lineHeight, configuredLineHeight) != 0) {
            trait.setLineHeight(configuredLineHeight);
            lineHeight = configuredLineHeight;
        }
        return trait;
    }

    private Location getDisplayLocation() {
        Location location = npc.getEntity().getLocation().clone();
        location.add(0, npc.getEntity().getHeight(), 0);
        return location;
    }

    @Override
    public void onSpawn() {
        active = true;
    }

    @Override
    public void onDespawn() {
        clearHolograms();
        active = false;
    }

    @Override
    public void onRemove() {
        clearHolograms();
    }

    private void clearHolograms() {
        if (displayNpc != null) {
            displayNpc.destroy();
            displayNpc = null;
        }
        displayedLineCount = 0;
        lineHeight = Double.NaN;
    }

    @Override
    public void load(DataKey key) {
        this.serverId = key.getString("serverId");
    }

    @Override
    public void save(DataKey key) {
        key.setString("serverId", serverId);
    }
}