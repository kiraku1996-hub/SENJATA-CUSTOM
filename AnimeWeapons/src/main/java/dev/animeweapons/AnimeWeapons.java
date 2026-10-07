package dev.animeweapons;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AnimeWeapons extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        WeaponManager weapons = new WeaponManager(this);
        AbilityManager abilities = new AbilityManager(this);

        getServer().getPluginManager().registerEvents(new AbilityListener(weapons, abilities), this);

        PluginCommand cmd = getCommand("animeweapons");
        if (cmd != null) {
            AnimeWeaponsCommand handler = new AnimeWeaponsCommand(this, weapons);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        getLogger().info("AnimeWeapons aktif - 5 senjata dimuat.");
    }
}
