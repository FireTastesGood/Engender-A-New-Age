package net.firetastesgood.ageofminecraft.entity.fusion;

import net.firetastesgood.ageofminecraft.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class FusionBrain {
    public static final String NBT_OWNER        = "FusionOwner";
    public static final String NBT_SITTING      = "FusionSitting";
    public static final String NBT_GAVE_XP      = "FusionGaveFirstXP";
    public static final String NBT_FIRST_XP     = "FusionFirstXP";
    public static final String NBT_ATK_OVERRIDE = "FusionAtkOverride";
    public static final String NBT_SPAWN_FX     = "FusionPlayedSpawnFX"; // ← new: one-time spawn sound

    /** Attach a consistent “tamed” brain: sit, follow, assist, basic melee + self-defense. */
    public static void attachCommonGoals(Mob mob, FusionData data) {
        if (mob == null || data == null) return;

        // Sit (highest priority)
        mob.goalSelector.addGoal(0, new SitGoal(mob, data));

        // Follow owner
        mob.goalSelector.addGoal(3, new FollowOwnerGoalLike(mob, data, 1.2D, 6.0F, 2.0F, 48.0F));

        // Melee + self-defense (for pathfinders)
        if (mob instanceof PathfinderMob pm) {
            mob.goalSelector.addGoal(4, new MeleeAttackGoal(pm, 1.2D, true));
            mob.targetSelector.addGoal(4, new HurtByTargetGoal(pm));
        }

        // Assist owner
        mob.targetSelector.addGoal(1, new OwnerHurtTargetGoalLike(mob, data));
        mob.targetSelector.addGoal(2, new OwnerHurtByTargetGoalLike(mob, data));
    }

    /* -------------------- Data -------------------- */
    public static class FusionData {
        private UUID owner;
        private boolean sitting;
        private boolean gaveFirstXp;
        private boolean playedSpawnFx; // ← new: persist that we played spawn sound
        private int firstSpawnXp;
        private double attackOverride = Double.NEGATIVE_INFINITY;

        public void save(CompoundTag tag) {
            if (owner != null) tag.putUUID(NBT_OWNER, owner);
            tag.putBoolean(NBT_SITTING, sitting);
            tag.putBoolean(NBT_GAVE_XP, gaveFirstXp);
            tag.putBoolean(NBT_SPAWN_FX, playedSpawnFx); // ← new
            tag.putInt(NBT_FIRST_XP, firstSpawnXp);
            if (attackOverride != Double.NEGATIVE_INFINITY) tag.putDouble(NBT_ATK_OVERRIDE, attackOverride);
        }

        public void load(CompoundTag tag) {
            if (tag.hasUUID(NBT_OWNER)) owner = tag.getUUID(NBT_OWNER);
            sitting = tag.getBoolean(NBT_SITTING);
            gaveFirstXp = tag.getBoolean(NBT_GAVE_XP);
            playedSpawnFx = tag.getBoolean(NBT_SPAWN_FX); // ← new
            firstSpawnXp = tag.getInt(NBT_FIRST_XP);
            if (tag.contains(NBT_ATK_OVERRIDE)) attackOverride = tag.getDouble(NBT_ATK_OVERRIDE);
        }

        public Optional<Player> getOwner(Level level) {
            if (owner == null || !(level instanceof ServerLevel sl)) return Optional.empty();
            return Optional.ofNullable(sl.getPlayerByUUID(owner));
        }

        public void setOwner(Player p) { owner = (p == null) ? null : p.getUUID(); }
        public boolean isSitting() { return sitting; }
        public void setSitting(boolean b) { sitting = b; }
        public boolean gaveFirstXp() { return gaveFirstXp; }
        public void setGaveFirstXp(boolean b) { gaveFirstXp = b; }
        public boolean playedSpawnFx() { return playedSpawnFx; }          // ← new
        public void setPlayedSpawnFx(boolean b) { playedSpawnFx = b; }     // ← new
        public int firstSpawnXp() { return firstSpawnXp; }
        public void setFirstSpawnXp(int v) { firstSpawnXp = v; }
        public boolean hasAttackOverride() { return attackOverride != Double.NEGATIVE_INFINITY; }
        public double attackOverride() { return attackOverride; }
        public void setAttackOverride(double v) { attackOverride = v; }
    }

    /* -------------------- Goals -------------------- */

    /** Simple “sit” that halts movement and persists while flagged. */
    public static class SitGoal extends Goal {
        private final Mob mob;
        private final FusionData data;

        public SitGoal(Mob mob, FusionData data) {
            this.mob = Objects.requireNonNull(mob, "mob");
            this.data = Objects.requireNonNull(data, "FusionData");
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
        }

        @Override public boolean canUse() { return data.isSitting(); }
        @Override public boolean canContinueToUse() { return data.isSitting(); }
        @Override public void start() { mob.getNavigation().stop(); }
        @Override public void stop() { mob.setTarget(null); }
    }

    /** Follow the owner; teleports if too far; respects sit, spectators, and sleepers. */
    public static class FollowOwnerGoalLike extends Goal {
        private final Mob mob;
        private final FusionData data;
        private final double speed;
        private final float startDist;
        private final float stopDist;
        private final float teleportDist;
        private int tpCooldown = 0;

        public FollowOwnerGoalLike(Mob mob, FusionData data, double speed, float startDist, float stopDist, float teleportDist) {
            this.mob = Objects.requireNonNull(mob, "mob");
            this.data = Objects.requireNonNull(data, "FusionData");
            this.speed = speed;
            this.startDist = startDist;
            this.stopDist = stopDist;
            this.teleportDist = teleportDist;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (data.isSitting()) return false;
            // do not follow while actively fighting
            if (mob.getTarget() != null && mob.getTarget().isAlive()) return false;
            var owner = data.getOwner(mob.level()).orElse(null);
            if (owner == null || owner.isSpectator() || owner.isSleeping()) return false;
            return mob.distanceTo(owner) > startDist;
        }

        @Override
        public boolean canContinueToUse() {
            if (data.isSitting()) return false;
            // keep follow paused during combat
            if (mob.getTarget() != null && mob.getTarget().isAlive()) return false;
            var owner = data.getOwner(mob.level()).orElse(null);
            if (owner == null || owner.isSpectator() || owner.isSleeping()) return false;
            return mob.distanceTo(owner) > stopDist;
        }

        @Override
        public void tick() {
            if (tpCooldown > 0) tpCooldown--;

            var owner = data.getOwner(mob.level()).orElse(null);
            if (owner == null) return;

            double dist = mob.distanceTo(owner);
            if (dist >= teleportDist && tpCooldown == 0) {
                if (tryTeleportNear(owner.blockPosition())) {
                    mob.getNavigation().stop();
                    tpCooldown = 20; // 1s cooldown
                    return;
                }
            }

            // avoid spamming path recalcs
            if (mob.getNavigation().isDone()) {
                mob.getNavigation().moveTo(owner, speed);
            }
            mob.getLookControl().setLookAt(owner, 10.0F, mob.getMaxHeadXRot());
        }

        private boolean tryTeleportNear(BlockPos base) {
            var cursor = new BlockPos.MutableBlockPos();
            for (int i = 0; i < 24; i++) {
                int dx = mob.getRandom().nextInt(5) - 2;
                int dz = mob.getRandom().nextInt(5) - 2;
                cursor.set(base.getX() + dx, base.getY(), base.getZ() + dz);
                if (isSafeTeleport(cursor)) {
                    mob.teleportTo(cursor.getX() + 0.5, cursor.getY(), cursor.getZ() + 0.5);
                    mob.resetFallDistance(); // ← new: prevent residual fall damage after teleport
                    return true;
                }
            }
            return false;
        }

        private boolean isSafeTeleport(BlockPos pos) {
            Level level = mob.level();
            // two blocks of headroom
            if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()) return false;
            // must stand on something solid
            if (level.getBlockState(pos.below()).isAir()) return false;
            // avoid fluids
            if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.above()).isEmpty()) return false;
            // collision clearance at destination
            var aabb = mob.getDimensions(Pose.STANDING).makeBoundingBox(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            return level.noCollision(mob, aabb);
        }
    }

    /** Attack what the owner is attacking, with safety checks. */
    public static class OwnerHurtTargetGoalLike extends Goal {
        private final Mob mob;
        private final FusionData data;
        private LivingEntity lastOwnerTarget;

        public OwnerHurtTargetGoalLike(Mob mob, FusionData data) {
            this.mob = Objects.requireNonNull(mob, "mob");
            this.data = Objects.requireNonNull(data, "FusionData");
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            var owner = data.getOwner(mob.level()).orElse(null);
            if (owner == null || data.isSitting()) return false;

            LivingEntity target = owner.getLastHurtMob();
            if (target == null || target == lastOwnerTarget || isBadTarget(target, owner)) return false;

            this.lastOwnerTarget = target;
            return true;
        }

        @Override
        public void start() {
            mob.setTarget(lastOwnerTarget);
            super.start();
        }

        @Override
        public void stop() {
            // clear correctly (don’t compare against null)
            LivingEntity prev = lastOwnerTarget;
            lastOwnerTarget = null;
            if (mob.getTarget() == prev) {
                mob.setTarget(null);
            }
        }
    }

    /** Attack who hurt the owner, with safety checks. */
    public static class OwnerHurtByTargetGoalLike extends Goal {
        private final Mob mob;
        private final FusionData data;
        private LivingEntity lastAttacker;

        public OwnerHurtByTargetGoalLike(Mob mob, FusionData data) {
            this.mob = Objects.requireNonNull(mob, "mob");
            this.data = Objects.requireNonNull(data, "FusionData");
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            var owner = data.getOwner(mob.level()).orElse(null);
            if (owner == null || data.isSitting()) return false;

            LivingEntity attacker = owner.getLastHurtByMob();
            if (attacker == null || attacker == lastAttacker || isBadTarget(attacker, owner)) return false;

            this.lastAttacker = attacker;
            return true;
        }

        @Override
        public void start() {
            mob.setTarget(lastAttacker);
            super.start();
        }

        @Override
        public void stop() {
            // clear correctly (don’t compare against null)
            LivingEntity prev = lastAttacker;
            lastAttacker = null;
            if (mob.getTarget() == prev) {
                mob.setTarget(null);
            }
        }
    }

    /* -------------------- Utils -------------------- */

    /** Award first-spawn XP once and play a one-time spawn sound (server-side only). */
    public static void maybeGiveFirstSpawnXP(Mob mob, FusionData data) {
        if (data == null) return;
        if (mob.level().isClientSide) return;

        ServerLevel sl = (ServerLevel) mob.level();

        // Play spawn sound once, independent of XP
        if (!data.playedSpawnFx()) {
            sl.playSound(
                    null,                      // all nearby players
                    mob.blockPosition(),
                    ModSounds.FUSION_SPAWN.get(),
                    SoundSource.NEUTRAL,
                    1.0f, 1.0f
            );
            data.setPlayedSpawnFx(true);
        }

        // Award first-spawn XP once
        if (!data.gaveFirstXp() && data.firstSpawnXp() > 0) {
            ExperienceOrb.award(sl, mob.position(), data.firstSpawnXp());
            data.setGaveFirstXp(true);
        }
    }

    /** Toggle sit and clear navigation/target when sitting. */
    public static void toggleSit(Mob mob, FusionData data) {
        if (data == null) return;
        boolean now = !data.isSitting();
        data.setSitting(now);
        if (now) {
            mob.getNavigation().stop();
            mob.setTarget(null);
        }
    }

    /** Shared target safety checks. */
    private static boolean isBadTarget(LivingEntity candidate, Player owner) {
        if (candidate == null || !candidate.isAlive()) return true;
        if (candidate == owner) return true;
        if (candidate instanceof Player p && p.isCreative()) return true;
        if (candidate.getTeam() != null && owner.getTeam() != null && candidate.getTeam().isAlliedTo(owner.getTeam())) return true;
        if (candidate instanceof OwnableFusion) return true; // don’t attack other fusions (esp. same owner)
        return false;
    }
}