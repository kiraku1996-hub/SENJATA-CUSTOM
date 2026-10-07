package dev.animeweapons;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public final class AbilityManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final AnimeWeapons plugin;
    private final Map<UUID, Map<WeaponType, Long>> cooldowns = new HashMap<>();

    public AbilityManager(AnimeWeapons plugin) {
        this.plugin = plugin;
    }

    public void clear(UUID uuid) {
        cooldowns.remove(uuid);
    }

    // ------------------------------------------------------------------
    // Entry point
    // ------------------------------------------------------------------

    public void use(Player p, WeaponType type) {
        if (!p.hasPermission("animeweapons.use")) return;

        long now = System.currentTimeMillis();
        Map<WeaponType, Long> map = cooldowns.computeIfAbsent(p.getUniqueId(), k -> new EnumMap<>(WeaponType.class));
        long ready = map.getOrDefault(type, 0L);
        if (now < ready) {
            double left = (ready - now) / 1000.0;
            p.sendActionBar(MM.deserialize("<red>" + type.abilityName() + " cooldown: <white>"
                    + String.format(Locale.US, "%.1f", left) + "s"));
            return;
        }

        double cd = cfg(type, "cooldown", 8.0);
        map.put(type, now + (long) (cd * 1000));
        p.sendActionBar(MM.deserialize("<gradient:#ff5252:#ffd740><bold>" + type.abilityName() + "!</bold></gradient>"));

        double dmg = cfg(type, "damage", 10.0);
        switch (type) {
            case DISMANTLE -> dismantle(p, dmg);
            case HOLLOW_PURPLE -> hollowPurple(p, dmg);
            case INVERTED_SPEAR -> heavenlyDash(p, dmg);
            case SUN_BREATHING -> sunBreathing(p, dmg);
            case GETSUGA -> getsuga(p, dmg);
        }
    }

    private double cfg(WeaponType t, String key, double def) {
        return plugin.getConfig().getDouble("weapons." + t.id() + "." + key, def);
    }

    // ------------------------------------------------------------------
    // 1) Sukuna - Dismantle
    // ------------------------------------------------------------------

    private void dismantle(Player p, double dmg) {
        World w = p.getWorld();
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        Vector side = perpendicular(dir);
        // garis tebasan diagonal
        Vector slash = side.clone().multiply(0.7).add(new Vector(0, 0.7, 0));
        Particle.DustOptions red = new Particle.DustOptions(Color.fromRGB(200, 0, 30), 1.4f);
        Set<UUID> hit = new HashSet<>();

        w.playSound(eye, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.4f, 0.6f);
        w.playSound(eye, Sound.ENTITY_WITHER_SHOOT, 0.6f, 1.8f);

        for (double d = 1.0; d <= 14.0; d += 0.5) {
            Location pt = eye.clone().add(dir.clone().multiply(d));
            if (!pt.getBlock().isPassable()) break;

            for (double k = -2.5; k <= 2.5; k += 0.5) {
                Location s = pt.clone().add(slash.clone().multiply(k));
                w.spawnParticle(Particle.DUST, s, 1, 0, 0, 0, 0, red);
            }
            if (((int) (d * 2)) % 4 == 0) {
                w.spawnParticle(Particle.SWEEP_ATTACK, pt, 1, 0, 0, 0, 0);
            }

            for (LivingEntity e : w.getNearbyLivingEntities(pt, 1.8)) {
                if (valid(p, e) && hit.add(e.getUniqueId())) {
                    e.setNoDamageTicks(0);
                    e.damage(dmg, p);
                    w.spawnParticle(Particle.CRIT, e.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.2);
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // 2) Gojo - Hollow Purple
    // ------------------------------------------------------------------

    private static final int PURPLE_CHARGE_TICKS = 25;

    private void hollowPurple(Player p, double dmg) {
        World w = p.getWorld();
        w.playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.5f, 0.8f);
        Particle.DustOptions blue = new Particle.DustOptions(Color.fromRGB(40, 120, 255), 1.6f);
        Particle.DustOptions red = new Particle.DustOptions(Color.fromRGB(255, 30, 60), 1.6f);

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (!p.isOnline() || p.isDead()) {
                    cancel();
                    return;
                }
                Location eye = p.getEyeLocation();
                Vector dir = eye.getDirection().normalize();
                Vector side = perpendicular(dir);
                Location center = eye.clone().add(dir.clone().multiply(2.0));
                double spread = 1.6 * (1.0 - (double) t / PURPLE_CHARGE_TICKS);

                Location b = center.clone().add(side.clone().multiply(spread));
                Location r = center.clone().subtract(side.clone().multiply(spread));
                w.spawnParticle(Particle.DUST, b, 8, 0.12, 0.12, 0.12, 0, blue);
                w.spawnParticle(Particle.DUST, r, 8, 0.12, 0.12, 0.12, 0, red);

                if (++t >= PURPLE_CHARGE_TICKS) {
                    cancel();
                    launchPurple(p, dmg);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void launchPurple(Player p, double dmg) {
        World w = p.getWorld();
        Vector dir = p.getEyeLocation().getDirection().normalize();
        Location start = p.getEyeLocation().add(dir.clone().multiply(2.0));
        Particle.DustOptions purple = new Particle.DustOptions(Color.fromRGB(170, 0, 255), 2.2f);

        w.playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.6f, 0.8f);

        new BukkitRunnable() {
            final Location loc = start.clone();
            final Set<UUID> hit = new HashSet<>();
            int t = 0;

            @Override
            public void run() {
                loc.add(dir);
                t++;

                w.spawnParticle(Particle.DUST, loc, 30, 0.5, 0.5, 0.5, 0, purple);
                w.spawnParticle(Particle.REVERSE_PORTAL, loc, 12, 0.4, 0.4, 0.4, 0.1);
                w.spawnParticle(Particle.END_ROD, loc, 3, 0.3, 0.3, 0.3, 0.02);

                for (LivingEntity e : w.getNearbyLivingEntities(loc, 2.5)) {
                    if (valid(p, e) && hit.add(e.getUniqueId())) {
                        e.setNoDamageTicks(0);
                        e.damage(dmg, p);
                    }
                }

                if (!loc.getBlock().isPassable() || t >= 60 || !p.isOnline()) {
                    cancel();
                    purpleExplosion(p, loc.clone(), dmg * 0.5, hit);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void purpleExplosion(Player p, Location loc, double dmg, Set<UUID> alreadyHit) {
        World w = loc.getWorld();
        w.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);
        w.spawnParticle(Particle.DUST, loc, 150, 2.5, 2.5, 2.5, 0,
                new Particle.DustOptions(Color.fromRGB(170, 0, 255), 2.5f));
        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.6f);

        for (LivingEntity e : w.getNearbyLivingEntities(loc, 5.0)) {
            if (!valid(p, e) || alreadyHit.contains(e.getUniqueId())) continue;
            e.setNoDamageTicks(0);
            e.damage(dmg, p);
            knockback(e, loc, 1.2);
        }

        if (plugin.getConfig().getBoolean("weapons.hollow_purple.break-blocks", false)) {
            int r = plugin.getConfig().getInt("weapons.hollow_purple.break-radius", 4);
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        if (x * x + y * y + z * z > r * r) continue;
                        Block b = loc.clone().add(x, y, z).getBlock();
                        if (b.getType().isAir() || b.isLiquid() || b.getType().getHardness() < 0) continue;
                        BlockBreakEvent ev = new BlockBreakEvent(b, p);
                        Bukkit.getPluginManager().callEvent(ev);
                        if (!ev.isCancelled()) b.setType(Material.AIR);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // 3) Toji - Inverted Spear of Heaven (Heavenly Dash)
    // ------------------------------------------------------------------

    private void heavenlyDash(Player p, double dmg) {
        double trueDmg = cfg(WeaponType.INVERTED_SPEAR, "true-damage", 4.0);
        World w = p.getWorld();

        Vector d = p.getLocation().getDirection().setY(0);
        if (d.lengthSquared() < 1.0E-4) d = p.getEyeLocation().getDirection().setY(0).add(new Vector(1, 0, 0));
        d.normalize().multiply(1.9).setY(0.25);
        p.setVelocity(d);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));
        w.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 1.4f);
        w.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.7f, 1.5f);

        new BukkitRunnable() {
            final Set<UUID> hit = new HashSet<>();
            int t = 0;

            @Override
            public void run() {
                if (!p.isOnline() || p.isDead()) {
                    cancel();
                    return;
                }
                Location c = p.getLocation().add(0, 1, 0);
                w.spawnParticle(Particle.CRIT, c, 8, 0.3, 0.4, 0.3, 0.1);
                w.spawnParticle(Particle.SWEEP_ATTACK, c, 1, 0, 0, 0, 0);

                for (LivingEntity e : w.getNearbyLivingEntities(c, 2.2)) {
                    if (valid(p, e) && hit.add(e.getUniqueId())) {
                        e.setNoDamageTicks(0);
                        e.damage(dmg, p);
                        trueDamage(e, p, trueDmg);
                        w.spawnParticle(Particle.CRIT, e.getLocation().add(0, 1, 0), 25, 0.3, 0.5, 0.3, 0.3);
                        w.playSound(e.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 0.8f);
                    }
                }
                if (++t >= 10) cancel();
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    // ------------------------------------------------------------------
    // 4) Tanjiro - Sun Breathing: Dance of the Fire God
    // ------------------------------------------------------------------

    private void sunBreathing(Player p, double dmg) {
        World w = p.getWorld();
        p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 0));
        w.playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.5f, 0.7f);
        Particle.DustOptions orange = new Particle.DustOptions(Color.fromRGB(255, 140, 0), 1.5f);

        new BukkitRunnable() {
            final Set<UUID> hit = new HashSet<>();
            int t = 0;

            @Override
            public void run() {
                if (!p.isOnline() || p.isDead()) {
                    cancel();
                    return;
                }
                Location c = p.getLocation().add(0, 1, 0);
                double r = 1.0 + t * 0.4;
                int points = 28;
                for (int i = 0; i < points; i++) {
                    double a = 2 * Math.PI * i / points + t * 0.3;
                    Location pt = c.clone().add(Math.cos(a) * r, 0, Math.sin(a) * r);
                    w.spawnParticle(Particle.FLAME, pt, 1, 0, 0, 0, 0.02);
                    w.spawnParticle(Particle.DUST, pt.clone().add(0, -0.5, 0), 1, 0, 0, 0, 0, orange);
                }
                if (t % 3 == 0) w.playSound(c, Sound.ENTITY_BLAZE_SHOOT, 0.8f, 1.2f);

                for (LivingEntity e : w.getNearbyLivingEntities(c, r + 0.8)) {
                    if (valid(p, e) && hit.add(e.getUniqueId())) {
                        e.setNoDamageTicks(0);
                        e.damage(dmg, p);
                        e.setFireTicks(80);
                        knockback(e, c, 0.9);
                    }
                }
                if (++t >= 10) cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ------------------------------------------------------------------
    // 5) Ichigo - Getsuga Tenshou
    // ------------------------------------------------------------------

    private void getsuga(Player p, double dmg) {
        World w = p.getWorld();
        Vector dir = p.getEyeLocation().getDirection().normalize();
        Vector axis = Math.abs(dir.getY()) > 0.95 ? perpendicular(dir) : new Vector(0, 1, 0);
        Location start = p.getEyeLocation().add(dir.clone().multiply(1.5));
        Particle.DustOptions black = new Particle.DustOptions(Color.fromRGB(15, 15, 15), 1.8f);
        Particle.DustOptions crimson = new Particle.DustOptions(Color.fromRGB(190, 0, 20), 1.4f);

        w.playSound(start, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.2f, 1.3f);
        w.playSound(start, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 0.5f);

        new BukkitRunnable() {
            final Location loc = start.clone();
            final Set<UUID> hit = new HashSet<>();
            int t = 0;

            @Override
            public void run() {
                loc.add(dir.clone().multiply(1.2));
                t++;

                for (double k = -1.0; k <= 1.0; k += 0.1) {
                    Vector off = axis.clone().multiply(2.4 * k).add(dir.clone().multiply(1.0 * (1 - k * k)));
                    Location pt = loc.clone().add(off);
                    w.spawnParticle(Particle.DUST, pt, 1, 0.05, 0.05, 0.05, 0, black);
                    w.spawnParticle(Particle.DUST, pt, 1, 0.05, 0.05, 0.05, 0, crimson);
                }
                w.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 4, 0.5, 0.8, 0.5, 0.02);

                for (LivingEntity e : w.getNearbyLivingEntities(loc, 2.3)) {
                    if (valid(p, e) && hit.add(e.getUniqueId())) {
                        e.setNoDamageTicks(0);
                        e.damage(dmg, p);
                        knockback(e, loc.clone().subtract(dir), 1.0);
                    }
                }

                if (!loc.getBlock().isPassable() || t >= 25) {
                    cancel();
                    w.spawnParticle(Particle.EXPLOSION, loc, 3, 0.5, 0.5, 0.5, 0);
                    w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.4f);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private boolean valid(Player caster, LivingEntity e) {
        if (e.equals(caster) || e.isDead() || e instanceof ArmorStand) return false;
        if (e instanceof Player target) {
            GameMode gm = target.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return false;
            if (!target.getWorld().getPVP()) return false;
        }
        return true;
    }

    private void trueDamage(LivingEntity e, Player src, double amount) {
        if (e.isDead()) return;
        double h = e.getHealth();
        if (h - amount <= 0) {
            e.damage(100000.0, src);
        } else {
            e.setHealth(h - amount);
        }
    }

    private void knockback(LivingEntity e, Location from, double power) {
        Vector kb = e.getLocation().toVector().subtract(from.toVector()).setY(0);
        if (kb.lengthSquared() < 1.0E-4) kb = new Vector(0, 0, 0);
        else kb.normalize().multiply(power);
        kb.setY(0.35);
        e.setVelocity(kb);
    }

    private static Vector perpendicular(Vector dir) {
        Vector v = dir.clone().crossProduct(new Vector(0, 1, 0));
        if (v.lengthSquared() < 1.0E-4) v = new Vector(1, 0, 0);
        return v.normalize();
    }
}
