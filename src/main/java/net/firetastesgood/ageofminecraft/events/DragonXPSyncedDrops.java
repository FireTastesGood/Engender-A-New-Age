package net.firetastesgood.ageofminecraft.events;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.firetastesgood.ageofminecraft.entity.EntropyOrbEntity;
import net.firetastesgood.ageofminecraft.entity.ManaOrbEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "ageofminecraft", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DragonXPSyncedDrops {
    private static final Logger LOG = LogUtils.getLogger();

    private static final boolean REQUIRE_ATTACKER       = false;
    private static final boolean REQUIRE_NEARBY_PLAYER  = false;
    private static final int    NEARBY_PLAYER_RADIUS    = 24;

    private static final int EARLIEST_TICKS = 120;
    private static final int WINDOW_TICKS   = 600;

    private static final int MAX_ORB_VALUE      = 64;
    private static final double SPREAD_RADIUS   = 1.2D;
    private static final double VERTICAL_LIFT   = 0.35D;
    private static final double BASE_UPWARD_VEL = 0.08D;
    private static final double HORIZ_SPEED     = 0.12D;
    private static final double JITTER_SPEED    = 0.02D;

    private static final RandomSource RNG = RandomSource.create();

    private static final class Pending {
        final UUID dragonId;
        final ServerLevel level;
        final int earliestTick, expiresTick;
        final int manaTotal, entropyTotal;
        boolean triggered = false;

        Pending(UUID id, ServerLevel lvl, int earliest, int expires, int mana, int entropy) {
            this.dragonId = id; this.level = lvl;
            this.earliestTick = earliest; this.expiresTick = expires;
            this.manaTotal = mana; this.entropyTotal = entropy;
        }
    }
    private static final Map<UUID, Pending> PENDING = new HashMap<>();

    private DragonXPSyncedDrops() {}

    @SubscribeEvent
    public static void onDragonDeath(LivingDeathEvent e) {
        if (!(e.getEntity() instanceof EnderDragon dragon)) return;
        if (!(dragon.level() instanceof ServerLevel level)) return;
        if (level.isClientSide) return;

        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            LOG.debug("[DragonDrops] Skip: doMobLoot=false");
            return;
        }

        if (REQUIRE_ATTACKER) {
            Entity attacker = e.getSource().getEntity();
            if (attacker == null) { LOG.debug("[DragonDrops] Skip: attacker=null"); return; }
            if (attacker instanceof LivingEntity && dragon.isAlliedTo((LivingEntity) attacker)) {
                LOG.debug("[DragonDrops] Skip: attacker allied");
                return;
            }
        }
        if (REQUIRE_NEARBY_PLAYER) {
            Player nearby = level.getNearestPlayer(dragon, NEARBY_PLAYER_RADIUS);
            if (nearby == null) { LOG.debug("[DragonDrops] Skip: no player within {} blocks", NEARBY_PLAYER_RADIUS); return; }
        }

        float maxHealth   = dragon.getMaxHealth();
        int manaTotal     = (int) maxHealth;
        int entropyTotal  = (maxHealth >= 100.0F) ? (int)(maxHealth * 0.10F) : 0;

        int now = level.getServer().getTickCount();
        PENDING.put(dragon.getUUID(),
                new Pending(dragon.getUUID(), level, now + EARLIEST_TICKS, now + WINDOW_TICKS, manaTotal, entropyTotal));

        LOG.debug("[DragonDrops] Armed for dragon={} now={} earliest={} expires={} totals=[mana={}, entropy={}]",
                dragon.getUUID(), now, now + EARLIEST_TICKS, now + WINDOW_TICKS, manaTotal, entropyTotal);
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide()) return;
        if (!(e.getEntity() instanceof ExperienceOrb xp)) return;

        ServerLevel level = (ServerLevel) e.getLevel();
        int now = level.getServer().getTickCount();

        Pending hit = null;
        for (Pending p : PENDING.values()) {
            if (p.level == level && !p.triggered && now >= p.earliestTick && now <= p.expiresTick) {
                hit = p; break;
            }
        }
        if (hit == null) return;

        hit.triggered = true;
        PENDING.remove(hit.dragonId);

        double x = xp.getX(), y = xp.getY(), z = xp.getZ();
        LOG.debug("[DragonDrops] XP observed at {},{},{} (tick {}): spawning mana/entropy", x, y, z, now);

        int manaLeft = hit.manaTotal;
        while (manaLeft > 0) {
            int split = Math.min(MAX_ORB_VALUE, splitXP(manaLeft));
            manaLeft -= split;
            spawnMana(level, x, y, z, split);
        }

        int entropyLeft = hit.entropyTotal;
        while (entropyLeft > 0) {
            int split = Math.min(MAX_ORB_VALUE, splitXP(entropyLeft));
            entropyLeft -= split;
            spawnEntropy(level, x, y, z, split);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        int now = e.getServer().getTickCount();
        PENDING.values().removeIf(p -> p.triggered || now > p.expiresTick);
    }

    private static void spawnMana(ServerLevel level, double x, double y, double z, int value) {
        double angle  = RNG.nextDouble() * Math.PI * 2.0;
        double radius = Math.sqrt(RNG.nextDouble()) * SPREAD_RADIUS;
        double ox = Math.cos(angle) * radius;
        double oz = Math.sin(angle) * radius;
        double oy = RNG.nextDouble() * VERTICAL_LIFT;

        double vx = (ox / Math.max(0.001D, SPREAD_RADIUS)) * HORIZ_SPEED + (RNG.nextDouble() - 0.5D) * JITTER_SPEED;
        double vz = (oz / Math.max(0.001D, SPREAD_RADIUS)) * HORIZ_SPEED + (RNG.nextDouble() - 0.5D) * JITTER_SPEED;
        double vy = BASE_UPWARD_VEL + RNG.nextDouble() * (BASE_UPWARD_VEL * 0.5D);

        ManaOrbEntity orb = new ManaOrbEntity(level, x + ox, y + oy, z + oz, value);
        orb.setDeltaMovement(vx, vy, vz);
        level.addFreshEntity(orb);
    }

    private static void spawnEntropy(ServerLevel level, double x, double y, double z, int value) {
        double angle  = RNG.nextDouble() * Math.PI * 2.0;
        double radius = Math.sqrt(RNG.nextDouble()) * SPREAD_RADIUS;
        double ox = Math.cos(angle) * radius;
        double oz = Math.sin(angle) * radius;
        double oy = RNG.nextDouble() * VERTICAL_LIFT;

        double vx = (ox / Math.max(0.001D, SPREAD_RADIUS)) * HORIZ_SPEED + (RNG.nextDouble() - 0.5D) * JITTER_SPEED;
        double vz = (oz / Math.max(0.001D, SPREAD_RADIUS)) * HORIZ_SPEED + (RNG.nextDouble() - 0.5D) * JITTER_SPEED;
        double vy = BASE_UPWARD_VEL + RNG.nextDouble() * (BASE_UPWARD_VEL * 0.5D);

        EntropyOrbEntity orb = new EntropyOrbEntity(level, x + ox, y + oy, z + oz, value);
        orb.setDeltaMovement(vx, vy, vz);
        level.addFreshEntity(orb);
    }

    private static int splitXP(int value) {
        if (value >= 1024) return 1024;
        if (value >= 512)  return 512;
        if (value >= 256)  return 256;
        if (value >= 128)  return 128;
        if (value >= 64)   return 64;
        if (value >= 32)   return 32;
        if (value >= 16)   return 16;
        if (value >= 8)    return 8;
        if (value >= 4)    return 4;
        return value >= 2 ? 2 : 1;
    }
}