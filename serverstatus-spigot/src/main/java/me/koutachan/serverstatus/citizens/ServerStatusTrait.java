package me.koutachan.serverstatus.citizens;

import me.koutachan.serverstatus.ServerStatusSpigot;
import me.koutachan.serverstatus.ServerStatusInfo;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@TraitName("server-status")
public class ServerStatusTrait extends Trait {
    private String serverId;
    private List<HologramTrait.ArmorstandRenderer> renderers = new ArrayList<>();
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
        String[] texts;
        if (content instanceof List) {
            texts = ((List<String>) content).toArray(new String[0]);
        } else {
            texts = content.toString().split("\n");
        }

        List<HologramTrait.ArmorstandRenderer> newRenderers = new ArrayList<>();

        HologramTrait trait = npc.getTraitNullable(HologramTrait.class);

        try {
            Field linesField = HologramTrait.class.getDeclaredField("lines");
            linesField.setAccessible(true);
            List<Object> hologramLines = (List<Object>) linesField.get(trait);

            for (int i = texts.length - 1; i >= 0; i--) {
                String formattedText = formatter.apply(ChatColor.translateAlternateColorCodes('&', texts[i]));
                HologramTrait.ArmorstandRenderer renderer;

                if (i < renderers.size()) {
                    renderer = renderers.get(i);

                    hologramLines.stream()
                            .filter(line -> getRendererFromLine(line) == renderer)
                            .findFirst()
                            .ifPresent(line -> setLineText(line, formattedText));

                    renderer.updateText(npc, formattedText);
                } else {
                    renderer = new HologramTrait.ArmorstandRenderer();
                    trait.addTemporaryLine(formattedText, -1, renderer);
                }

                newRenderers.add(0, renderer);
            }

            // 未使用のレンダラーを削除
            List<HologramTrait.ArmorstandRenderer> renderersToRemove = new ArrayList<>(renderers);
            renderersToRemove.removeAll(newRenderers);

            if (!renderersToRemove.isEmpty()) {
                // 未使用レンダラーとそれに関連するホログラムラインを削除
                for (HologramTrait.ArmorstandRenderer oldRenderer : renderersToRemove) {
                    oldRenderer.destroy();
                    hologramLines.removeIf(line -> getRendererFromLine(line) == oldRenderer);
                }

                // ホログラムを再読み込み
                Method reloadMethod = HologramTrait.class.getDeclaredMethod("reloadLineHolograms");
                reloadMethod.setAccessible(true);
                reloadMethod.invoke(trait);
            }

            this.renderers = newRenderers;
        } catch (Exception ex) {
            throw new RuntimeException("ホログラム更新中にエラーが発生", ex);
        }
    }

    private Object getRendererFromLine(Object line) {
        try {
            Field rendererField = line.getClass().getDeclaredField("renderer");
            rendererField.setAccessible(true);
            return rendererField.get(line);
        } catch (Exception e) {
            throw new RuntimeException("レンダラー取得中にエラーが発生", e);
        }
    }

    private void setLineText(Object line, String text) {
        try {
            Method setTextMethod = line.getClass().getDeclaredMethod("setText", String.class);
            setTextMethod.setAccessible(true);
            setTextMethod.invoke(line, text);
        } catch (Exception e) {
            throw new RuntimeException("テキスト設定中にエラーが発生", e);
        }
    }

    @Override
    public void onSpawn() {
        active = true;
        HologramTrait trait = npc.getOrAddTrait(HologramTrait.class);
        try {
            Field heightField = HologramTrait.class.getDeclaredField("lineHeight");
            heightField.setAccessible(true);
            heightField.set(trait, ServerStatusSpigot.INSTANCE.getConfig().getDouble("display-height", 0.3));
        } catch (Exception ex) {
            throw new RuntimeException("ホログラム高さ設定中にエラーが発生", ex);
        }
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

    @SuppressWarnings("unchecked")
    private void clearHolograms() {
        if (renderers.isEmpty()) return;

        HologramTrait trait = npc.getTraitNullable(HologramTrait.class);
        if (trait == null) return;

        try {
            Field linesField = HologramTrait.class.getDeclaredField("lines");
            linesField.setAccessible(true);
            List<Object> hologramLines = (List<Object>) linesField.get(trait);

            for (HologramTrait.ArmorstandRenderer renderer : renderers) {
                renderer.destroy();
                hologramLines.removeIf(line -> getRendererFromLine(line) == renderer);
            }

            Method reloadMethod = HologramTrait.class.getDeclaredMethod("reloadLineHolograms");
            reloadMethod.setAccessible(true);
            reloadMethod.invoke(trait);

            renderers.clear();
        } catch (Exception ex) {
            throw new RuntimeException("ホログラム削除中にエラーが発生", ex);
        }
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