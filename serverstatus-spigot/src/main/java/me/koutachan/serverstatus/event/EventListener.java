package me.koutachan.serverstatus.event;

import me.koutachan.serverstatus.ServerStatusSpigot;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.function.Consumer;

public class EventListener implements Listener {
    @EventHandler
    public void onNPCRightClickEvent(NPCRightClickEvent event) {
        Consumer<NPC> con = ServerStatusSpigot.INSTANCE.queue.remove(event.getClicker().getUniqueId());
        if (con != null) {
            con.accept(event.getNPC());
        }
    }
}