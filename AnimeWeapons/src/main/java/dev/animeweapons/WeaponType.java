package dev.animeweapons;

import org.bukkit.Material;

import java.util.List;

public enum WeaponType {

    DISMANTLE("dismantle", Material.NETHERITE_SWORD,
            "<gradient:#8b0000:#ff1744><bold>Malevolent Dismantle</bold></gradient>",
            "Dismantle",
            List.of("<dark_gray>Jujutsu Kaisen",
                    "<gray>Pedang milik Raja Kutukan.",
                    "",
                    "<red>Klik Kanan: <white>Dismantle",
                    "<gray>Tebasan tak kasat mata menembus semua",
                    "<gray>makhluk di depanmu.")),

    HOLLOW_PURPLE("hollow_purple", Material.HEART_OF_THE_SEA,
            "<gradient:#2979ff:#d500f9><bold>Limitless: Hollow Purple</bold></gradient>",
            "Hollow Purple",
            List.of("<dark_gray>Jujutsu Kaisen",
                    "<gray>Orb Tak Terbatas milik yang terkuat.",
                    "",
                    "<light_purple>Klik Kanan: <white>Hollow Purple",
                    "<gray>Gabungkan Biru dan Merah, lalu",
                    "<gray>lenyapkan segalanya.")),

    INVERTED_SPEAR("inverted_spear", Material.IRON_SWORD,
            "<gradient:#78909c:#eceff1><bold>Inverted Spear of Heaven</bold></gradient>",
            "Heavenly Dash",
            List.of("<dark_gray>Jujutsu Kaisen",
                    "<gray>Senjata Pembunuh Penyihir.",
                    "",
                    "<white>Klik Kanan: <gray>Heavenly Dash",
                    "<gray>Menerjang cepat dan menusuk.",
                    "<gray>Sebagian damage menembus armor.")),

    SUN_BREATHING("sun_breathing", Material.DIAMOND_SWORD,
            "<gradient:#ff6d00:#ffd600><bold>Nichirin: Sun Breathing</bold></gradient>",
            "Dance of the Fire God",
            List.of("<dark_gray>Demon Slayer",
                    "<gray>Bilah hitam yang membara.",
                    "",
                    "<gold>Klik Kanan: <white>Dance of the Fire God",
                    "<gray>Gelombang api berputar membakar",
                    "<gray>semua musuh di sekitarmu.")),

    GETSUGA("getsuga", Material.DIAMOND_AXE,
            "<gradient:#212121:#d50000><bold>Zangetsu: Getsuga Tenshou</bold></gradient>",
            "Getsuga Tenshou",
            List.of("<dark_gray>Bleach",
                    "<gray>Pedang raksasa seorang Shinigami pengganti.",
                    "",
                    "<dark_red>Klik Kanan: <white>Getsuga Tenshou",
                    "<gray>Lepaskan bulan sabit energi hitam",
                    "<gray>yang menembus musuh."));

    private final String id;
    private final Material material;
    private final String displayName;
    private final String abilityName;
    private final List<String> lore;

    WeaponType(String id, Material material, String displayName, String abilityName, List<String> lore) {
        this.id = id;
        this.material = material;
        this.displayName = displayName;
        this.abilityName = abilityName;
        this.lore = lore;
    }

    public String id() { return id; }
    public Material material() { return material; }
    public String displayName() { return displayName; }
    public String abilityName() { return abilityName; }
    public List<String> lore() { return lore; }

    public static WeaponType byId(String id) {
        if (id == null) return null;
        for (WeaponType t : values()) {
            if (t.id.equalsIgnoreCase(id)) return t;
        }
        return null;
    }
}
