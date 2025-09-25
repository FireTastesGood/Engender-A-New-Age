package net.firetastesgood.ageofminecraft.events;

import net.firetastesgood.ageofminecraft.entity.EntropyOrbEntity;
import net.firetastesgood.ageofminecraft.entity.ManaOrbEntity;
import net.firetastesgood.ageofminecraft.EngenderMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

@Mod.EventBusSubscriber(modid = EngenderMod.MODID)
public class MobDropsHandler {

    private static final int MAX_ORB_VALUE = 64;

    private static final double SPREAD_RADIUS   = 1.2D;
    private static final double VERTICAL_LIFT   = 0.35D;
    private static final double BASE_UPWARD_VEL = 0.08D;
    private static final double HORIZ_SPEED     = 0.12D;
    private static final double JITTER_SPEED    = 0.02D;

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) return;
        if (victim instanceof EnderDragon) return;

        Entity attacker = event.getSource().getEntity();
        if (attacker == null) return;
        if (attacker instanceof LivingEntity && victim.isAlliedTo((LivingEntity) attacker)) return;
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;

        Player nearby = level.getNearestPlayer(victim, 24.0);
        if (nearby == null) return;

        float maxHealth = victim.getMaxHealth();
        int base = (victim instanceof AgeableMob) ? (int)(maxHealth * 0.25F) : (int)maxHealth;

        final double x = victim.getX();
        final double y = victim.getY() + victim.getBbHeight() * 0.5;
        final double z = victim.getZ();
        final RandomSource rng = level.getRandom();

        // Mana
        int mana = base;
        while (mana > 0) {
            int split = Math.min(MAX_ORB_VALUE, splitXP(mana));
            mana -= split;

            double angle  = rng.nextDouble() * Math.PI * 2.0;
            double radius = Math.sqrt(rng.nextDouble()) * SPREAD_RADIUS;
            double ox = Math.cos(angle) * radius;
            double oz = Math.sin(angle) * radius;
            double oy = rng.nextDouble() * VERTICAL_LIFT;

            double vx = (ox / Math.max(0.001D, SPREAD_RADIUS)) * HORIZ_SPEED + (rng.nextDouble() - 0.5D) * JITTER_SPEED;
            double vz = (oz / Math.max(0.001D, SPREAD_RADIUS)) * HORIZ_SPEED + (rng.nextDouble() - 0.5D) * JITTER_SPEED;
            double vy = BASE_UPWARD_VEL + rng.nextDouble() * (BASE_UPWARD_VEL * 0.5D);

            ManaOrbEntity orb = new ManaOrbEntity(level, x + ox, y + oy, z + oz, split);
            orb.setDeltaMovement(vx, vy, vz);
            level.addFreshEntity(orb);
        }

        // Entropy
        if (maxHealth >= 100.0F) {
            int entropy = (int)(maxHealth * 0.10F);
            while (entropy > 0) {
                int split = Math.min(MAX_ORB_VALUE, splitXP(entropy));
                entropy -= split;

                double angle  = rng.nextDouble() * Math.PI * 2.0;
                double radius = Math.sqrt(rng.nextDouble()) * SPREAD_RADIUS;
                double ox = Math.cos(angle) * radius;
                double oz = Math.sin(angle) * radius;
                double oy = rng.nextDouble() * VERTICAL_LIFT;

                double vx = (ox / Math.max(0.001D, SPREAD_RADIUS)) * HORIZ_SPEED + (rng.nextDouble() - 0.5D) * JITTER_SPEED;
                double vz = (oz / Math.max(0.001D, SPREAD_RADIUS)) * HORIZ_SPEED + (rng.nextDouble() - 0.5D) * JITTER_SPEED;
                double vy = BASE_UPWARD_VEL + rng.nextDouble() * (BASE_UPWARD_VEL * 0.5D);

                EntropyOrbEntity orb = new EntropyOrbEntity(level, x + ox, y + oy, z + oz, split);
                orb.setDeltaMovement(vx, vy, vz);
                level.addFreshEntity(orb);
            }
        }
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