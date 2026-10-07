package dev.animeweapons;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class AbilityListener implements Listener {

    private final WeaponManager weapons;
    private final AbilityManager abilities;

    public AbilityListener(WeaponManager weapons, AbilityManager abilities) {
        this.weapons = weapons;
        this.abilities = abilities;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

        WeaponType type = weapons.identify(e.getItem());
        if (type == null) return;

        e.setCancelled(true);
        Player p = e.getPlayer();
        abilities.use(p, type);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        abilities.clear(e.getPlayer().getUniqueId());
    }
}
