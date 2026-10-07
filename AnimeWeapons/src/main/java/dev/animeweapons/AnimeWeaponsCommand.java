package dev.animeweapons;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AnimeWeaponsCommand implements CommandExecutor, TabCompleter {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimeWeapons plugin;
    private final WeaponManager weapons;

    public AnimeWeaponsCommand(AnimeWeapons plugin, WeaponManager weapons) {
        this.plugin = plugin;
        this.weapons = weapons;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("animeweapons.admin")) {
            sender.sendMessage(MM.deserialize("<red>Kamu tidak punya izin."));
            return true;
        }
        if (args.length == 0) {
            help(sender, label);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "list" -> {
                sender.sendMessage(MM.deserialize("<gold>Senjata tersedia:"));
                for (WeaponType t : WeaponType.values()) {
                    sender.sendMessage(MM.deserialize("<gray> - <yellow>" + t.id() + " <dark_gray>(" + t.abilityName() + ")"));
                }
            }
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage(MM.deserialize("<green>Config dimuat ulang."));
            }
            case "give" -> {
                if (args.length < 2) {
                    help(sender, label);
                    return true;
                }
                // /aw give <weapon> [player]   atau   /aw give <player> <weapon>
                WeaponType type = WeaponType.byId(args[1]);
                Player target = null;
                if (type != null) {
                    if (args.length >= 3) target = Bukkit.getPlayerExact(args[2]);
                    else if (sender instanceof Player sp) target = sp;
                } else if (args.length >= 3) {
                    target = Bukkit.getPlayerExact(args[1]);
                    type = WeaponType.byId(args[2]);
                }
                if (type == null) {
                    sender.sendMessage(MM.deserialize("<red>Senjata tidak ditemukan. Gunakan /" + label + " list"));
                    return true;
                }
                if (target == null) {
                    sender.sendMessage(MM.deserialize("<red>Player tidak ditemukan / tidak online."));
                    return true;
               for (ItemStack i : left.values()) {
    target.getWorld().dropItemNaturally(target.getLocation(), i);
}
            }
            default -> help(sender, label);
        }
        return true;
    }

    private void help(CommandSender s, String label) {
        s.sendMessage(MM.deserialize("<gold>AnimeWeapons <gray>- perintah:"));
        s.sendMessage(MM.deserialize("<yellow>/" + label + " give <senjata> [player]"));
        s.sendMessage(MM.deserialize("<yellow>/" + label + " list"));
        s.sendMessage(MM.deserialize("<yellow>/" + label + " reload"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!sender.hasPermission("animeweapons.admin")) return out;

        if (args.length == 1) {
            for (String s : List.of("give", "list", "reload")) {
                if (s.startsWith(args[0].toLowerCase(Locale.ROOT))) out.add(s);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            for (WeaponType t : WeaponType.values()) {
                if (t.id().startsWith(args[1].toLowerCase(Locale.ROOT))) out.add(t.id());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(args[2].toLowerCase(Locale.ROOT))) out.add(p.getName());
            }
        }
        return out;
    }
}
