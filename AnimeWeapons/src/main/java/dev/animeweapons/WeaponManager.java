package dev.animeweapons;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class WeaponManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final NamespacedKey key;

    public WeaponManager(AnimeWeapons plugin) {
        this.key = new NamespacedKey(plugin, "weapon_id");
    }

    public ItemStack create(WeaponType type) {
        ItemStack item = new ItemStack(type.material());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(noItalic(MM.deserialize(type.displayName())));

        List<Component> lore = new ArrayList<>();
        for (String line : type.lore()) {
            lore.add(noItalic(MM.deserialize(line)));
        }
        meta.lore(lore);

        meta.setUnbreakable(true);
        meta.setEnchantmentGlintOverride(true);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, type.id());

        item.setItemMeta(meta);
        return item;
    }

    public WeaponType identify(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String id = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return WeaponType.byId(id);
    }

    private static Component noItalic(Component c) {
        return c.decoration(TextDecoration.ITALIC, false);
    }
}
