package net.firetastesgood.ageofminecraft.entity.fusion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;

public class FusionChickenEntity extends Chicken implements OwnableFusion {
    private FusionBrain.FusionData fusionData;

    public FusionChickenEntity(EntityType<? extends FusionChickenEntity> type, Level level) {
        super(type, level);
        setNoAi(false);
        setPersistenceRequired();
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return new ResourceLocation("minecraft", "entities/chicken");
    }

    @Override
    protected void registerGoals() {
        ensureFusionData();
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));

        removeGoals(this.goalSelector, PanicGoal.class, AvoidEntityGoal.class);

        this.goalSelector.addGoal(0, new FusionBrain.SitGoal(this, fusionData));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(4, new FusionBrain.FollowOwnerGoalLike(this, fusionData, 1.25D, 3.0F, 1.8F, 48.0F));

        this.targetSelector.addGoal(1, new FusionBrain.OwnerHurtTargetGoalLike(this, fusionData));
        this.targetSelector.addGoal(2, new FusionBrain.OwnerHurtByTargetGoalLike(this, fusionData));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));

        this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
    }

    private static void removeGoals(net.minecraft.world.entity.ai.goal.GoalSelector selector, Class<?>... types) {
        var copy = new java.util.ArrayList<>(selector.getAvailableGoals());
        for (var wrapped : copy) {
            var g = wrapped.getGoal();
            for (var t : types) if (t.isInstance(g)) selector.removeGoal(g);
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        ensureFusionData();

        if (fusionData.hasAttackOverride() && getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(fusionData.attackOverride());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ensureFusionData();
        fusionData.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ensureFusionData();

        fusionData.load(tag);

        if (tag.contains("FirstSpawnXP")) {
            fusionData.setFirstSpawnXp(tag.getInt("FirstSpawnXP"));
        } else if (tag.contains("first_spawn_xp")) {
            fusionData.setFirstSpawnXp(tag.getInt("first_spawn_xp"));
        }
        if (tag.contains("AttackDamageOverride")) {
            setFusionAttackOverride(tag.getDouble("AttackDamageOverride"));
        } else if (tag.contains("attack_damage_override")) {
            setFusionAttackOverride(tag.getDouble("attack_damage_override"));
        }
        if (tag.contains("fusion", 10)) {
            CompoundTag f = tag.getCompound("fusion");
            if (f.contains("FirstSpawnXP")) fusionData.setFirstSpawnXp(f.getInt("FirstSpawnXP"));
            else if (f.contains("first_spawn_xp")) fusionData.setFirstSpawnXp(f.getInt("first_spawn_xp"));

            if (f.contains("AttackDamageOverride")) setFusionAttackOverride(f.getDouble("AttackDamageOverride"));
            else if (f.contains("attack_damage_override")) setFusionAttackOverride(f.getDouble("attack_damage_override"));
        }

        if (fusionData.hasAttackOverride() && getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(fusionData.attackOverride());
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return super.mobInteract(player, hand);
        ensureFusionData();

        if (fusionData.getOwner(level()).isEmpty() && player.isShiftKeyDown()) {
            if (!level().isClientSide) {
                setFusionOwner(player);
                player.swing(hand, true);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        if (isOwnedBy(player) && player.getItemInHand(hand).isEmpty()) {
            if (!level().isClientSide) {
                FusionBrain.toggleSit(this, fusionData);
                boolean nowSitting = fusionData.isSitting();

                player.swing(hand, true);
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "message.ageofminecraft.fusion.state." + (nowSitting ? "sitting" : "following"),
                                this.getDisplayName()
                        ),
                        true
                );
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void setFusionOwner(Player p) {
        ensureFusionData();
        fusionData.setOwner(p);
    }

    @Override
    public void setFusionFirstSpawnXP(int xp) {
        ensureFusionData();
        fusionData.setFirstSpawnXp(xp);
    }

    @Override
    public void setFusionAttackOverride(double value) {
        ensureFusionData();
        fusionData.setAttackOverride(value);
        if (getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(value);
        }
    }

    private void ensureFusionData() {
        if (this.fusionData == null) this.fusionData = new FusionBrain.FusionData();
    }

    private boolean isOwnedBy(Player p) {
        ensureFusionData();
        return fusionData.getOwner(level())
                .map(o -> o.getUUID().equals(p.getUUID()))
                .orElse(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Chicken.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            ensureFusionData();
            FusionBrain.maybeGiveFirstSpawnXP(this, fusionData);
        }
    }

    @Override
    public Chicken getBreedOffspring(ServerLevel level, AgeableMob partner) {
        FusionChickenEntity baby = net.firetastesgood.ageofminecraft.registry.ModEntityTypes
                .CHICKEN_FUSION.get().create(level);
        if (baby != null) {
            Player p1 = (fusionData != null) ? fusionData.getOwner(level).orElse(null) : null;
            Player p2 = (partner instanceof FusionChickenEntity f2 && f2.fusionData != null)
                    ? f2.fusionData.getOwner(level).orElse(null) : null;

            Player picked = null;
            if (p1 != null && p2 != null) {
                picked = p1.getUUID().equals(p2.getUUID())
                        ? p1
                        : (this.getRandom().nextBoolean() ? p1 : p2);
            } else {
                picked = (p1 != null) ? p1 : p2;
            }

            if (picked != null) baby.setFusionOwner(picked);
        }
        return baby;
    }
}