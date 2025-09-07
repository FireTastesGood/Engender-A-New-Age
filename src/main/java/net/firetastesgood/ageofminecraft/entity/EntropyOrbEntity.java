package net.firetastesgood.ageofminecraft.entity;

import net.firetastesgood.ageofminecraft.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class EntropyOrbEntity extends Entity {

    // Merge tuning (closer to vanilla feel)
    private static final int MERGE_DELAY_TICKS = 70;    // ~2s before any merging is allowed
    private static final int MERGE_INTERVAL_TICKS = 20; // check for a merge once per second
    private static final double MERGE_RADIUS = 0.25D;   // very small radius so only truly touching orbs merge

    private static final EntityDataAccessor<Integer> DATA_VALUE =
            SynchedEntityData.defineId(EntropyOrbEntity.class, EntityDataSerializers.INT);

    public int getOrbSizeIndex() {
        int val = this.getValue();
        if (val >= 2477) return 10;
        if (val >= 1237) return 9;
        if (val >= 617)  return 8;
        if (val >= 307)  return 7;
        if (val >= 149)  return 6;
        if (val >= 73)   return 5;
        if (val >= 37)   return 4;
        if (val >= 17)   return 3;
        if (val >= 7)    return 2;
        return val >= 3 ? 1 : 0;
    }

    public EntropyOrbEntity(EntityType<? extends EntropyOrbEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
    }

    public EntropyOrbEntity(Level level, double x, double y, double z, int value) {
        this(ModEntityTypes.ENTROPY_ORB.get(), level);
        this.setPos(x, y, z);
        this.setValue(value);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_VALUE, 0);
    }

    public int getValue() {
        return this.entityData.get(DATA_VALUE);
    }

    public void setValue(int v) {
        this.entityData.set(DATA_VALUE, v);
    }

    @Override
    public void tick() {
        super.tick();

        // --- simple motion on BOTH sides (smooth visuals) ---
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
        }

        net.minecraft.world.phys.Vec3 motion = this.getDeltaMovement();
        this.move(net.minecraft.world.entity.MoverType.SELF, motion);

        final double airDrag = 0.98D;
        if (this.onGround()) {
            // no bounce on ground: zero Y, damp X/Z
            float blockFriction = this.level().getBlockState(this.blockPosition().below())
                    .getFriction(this.level(), this.blockPosition().below(), this);
            double friction = blockFriction * 0.91D;
            this.setDeltaMovement(motion.x * friction, 0.0D, motion.z * friction);
        } else {
            this.setDeltaMovement(motion.multiply(airDrag, 0.98D, airDrag));
        }

        // --- server-only logic (authoritative) ---
        if (!level().isClientSide) {
            if (this.tickCount >= MERGE_DELAY_TICKS && (this.tickCount % MERGE_INTERVAL_TICKS) == 0) {
                mergeNearby(MERGE_RADIUS);
            }
            if (this.tickCount >= 6000) {
                this.discard();
            }
        }
    }

    private void mergeNearby(double radius) {
        var aabb = this.getBoundingBox().inflate(radius);
        EntropyOrbEntity candidate = null;
        double bestDistSq = Double.MAX_VALUE;

        for (EntropyOrbEntity other : this.level().getEntitiesOfClass(
                EntropyOrbEntity.class, aabb,
                e -> e != this && e.isAlive() && e.tickCount >= MERGE_DELAY_TICKS)) {
            double d2 = this.distanceToSqr(other);
            if (d2 < bestDistSq) {
                bestDistSq = d2;
                candidate = other;
            }
        }

        if (candidate == null) return;

        EntropyOrbEntity keep   = (this.tickCount <= candidate.tickCount) ? this : candidate;
        EntropyOrbEntity remove = (keep == this) ? candidate : this;

        keep.setValue(keep.getValue() + remove.getValue());
        keep.tickCount = Math.min(keep.tickCount, remove.tickCount);

        if (!remove.isRemoved()) remove.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setValue(tag.getInt("Value"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Value", this.getValue());
        tag.putString("engender_type", "entropy");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}