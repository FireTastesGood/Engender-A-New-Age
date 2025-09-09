package net.firetastesgood.ageofminecraft.entity;

import net.firetastesgood.ageofminecraft.registry.ModEntityTypes;
import net.firetastesgood.ageofminecraft.util.CrystalHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public class ManaOrbEntity extends Entity {

    private static final int MAX_VALUE = 64;

    private static final int MERGE_DELAY_TICKS = 70;
    private static final int MERGE_INTERVAL_TICKS = 20;
    private static final double MERGE_RADIUS = 0.25D;

    private static final EntityDataAccessor<Integer> DATA_VALUE =
            SynchedEntityData.defineId(ManaOrbEntity.class, EntityDataSerializers.INT);

    public int getValue() {
        return this.entityData.get(DATA_VALUE);
    }

    public void setValue(int v) {
        this.entityData.set(DATA_VALUE, Math.max(0, v));
    }

    public ManaOrbEntity(EntityType<? extends ManaOrbEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
    }

    public ManaOrbEntity(Level level, double x, double y, double z, int value) {
        this(ModEntityTypes.MANA_ORB.get(), level);
        this.setPos(x, y, z);
        this.setValue(value);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_VALUE, 0);
    }

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

    @Override
    public void tick() {
        super.tick();

        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
        }

        Vec3 motion = this.getDeltaMovement();
        this.move(MoverType.SELF, motion);

        final double airDrag = 0.98D;
        if (this.onGround()) {
            double friction = this.level().getBlockState(this.blockPosition().below())
                    .getFriction(this.level(), this.blockPosition().below(), this) * 0.91D;
            this.setDeltaMovement(motion.x * friction, 0.0D, motion.z * friction);
        } else {
            this.setDeltaMovement(motion.multiply(airDrag, 0.98D, airDrag));
        }

        if (!level().isClientSide) {
            tryHomeToEligiblePlayer();

            if (this.tickCount >= MERGE_DELAY_TICKS && (this.tickCount % MERGE_INTERVAL_TICKS) == 0) {
                mergeNearby(MERGE_RADIUS);
            }

            // despawn 5 minutes
            if (this.tickCount >= 6000) {
                this.discard();
            }
        }
    }

    private void tryHomeToEligiblePlayer() {
        Player p = this.level().getNearestPlayer(this, 24.0D);
        if (p == null) return;

        if (CrystalHelper.findManaReceiverSlot(p) < 0) return;

        Vec3 to = new Vec3(
                p.getX() - this.getX(),
                (p.getY() + (double)p.getEyeHeight()) - this.getY(),
                p.getZ() - this.getZ()
        );
        double dist = to.length();
        if (dist < 1.0e-4D) return;

        double accel = 0.07D;
        double scale = 1.0D - Math.min(1.0D, dist / 12.0D);
        scale *= scale;
        this.setDeltaMovement(this.getDeltaMovement().add(to.normalize().scale(accel * scale)));
    }

    private void mergeNearby(double radius) {
        var aabb = this.getBoundingBox().inflate(radius);
        ManaOrbEntity candidate = null;
        double bestDistSq = Double.MAX_VALUE;

        for (ManaOrbEntity other : this.level().getEntitiesOfClass(
                ManaOrbEntity.class, aabb,
                e -> e != this && e.isAlive() && e.tickCount >= MERGE_DELAY_TICKS)) {
            double d2 = this.distanceToSqr(other);
            if (d2 < bestDistSq) {
                bestDistSq = d2;
                candidate = other;
            }
        }
        if (candidate == null) return;

        ManaOrbEntity keep   = (this.tickCount <= candidate.tickCount) ? this : candidate;
        ManaOrbEntity remove = (keep == this) ? candidate : this;

        int sum = keep.getValue() + remove.getValue();
        keep.tickCount = Math.min(keep.tickCount, remove.tickCount);

        if (sum <= MAX_VALUE) {
            keep.setValue(sum);
            if (!remove.isRemoved()) remove.discard();
        } else {
            keep.setValue(MAX_VALUE);
            int leftover = sum - MAX_VALUE;

            remove.setValue(leftover);
            remove.setPos(remove.getX(), remove.getY() + 0.01D, remove.getZ());
        }
    }
    @Override
    public void playerTouch(Player player) {
        if (this.level().isClientSide) return;
        if (this.isRemoved() || this.getValue() <= 0) return;

        int slot = CrystalHelper.findManaReceiverSlot(player);
        if (slot < 0) return;

        ItemStack receiver = player.getInventory().getItem(slot);
        int accepted = CrystalHelper.depositMana(receiver, this.getValue());
        if (accepted > 0) {
            this.setValue(this.getValue() - accepted);

            this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CHORUS_FLOWER_GROW, SoundSource.PLAYERS, 1.0F, 1.9F);

            if (this.getValue() <= 0) this.discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setValue(tag.getInt("Value"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Value", this.getValue());
        tag.putString("engender_type", "mana");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}