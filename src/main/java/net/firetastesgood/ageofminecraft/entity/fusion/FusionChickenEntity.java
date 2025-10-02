package net.firetastesgood.ageofminecraft.entity.fusion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;                     // ← added
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;               // ← added
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

public class FusionChickenEntity extends Chicken implements OwnableFusion {
    private FusionBrain.FusionData fusionData;

    public FusionChickenEntity(EntityType<? extends FusionChickenEntity> type, Level level) {
        super(type, level);
        setNoAi(false);
        setPersistenceRequired();
    }

    /* ---------------- Fall Damage & Loot ---------------- */

    // Chickens shouldn't take fall damage; make it explicit
    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    // Share vanilla chicken drops: minecraft:entities/chicken
    @Override
    protected ResourceLocation getDefaultLootTable() {
        return new ResourceLocation("minecraft", "entities/chicken");
    }

    /* ---------------- Goals ---------------- */

    @Override
    protected void registerGoals() {
        ensureFusionData(); // make sure it's ready inside Mob ctor chain
        super.registerGoals(); // keep vanilla basics

        // keep float
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // remove only goals that fight tamed/follow behavior
        removeGoals(this.goalSelector, PanicGoal.class, AvoidEntityGoal.class);

        // sit > melee > follow  (combat should outrank following)
        this.goalSelector.addGoal(0, new FusionBrain.SitGoal(this, fusionData));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(4, new FusionBrain.FollowOwnerGoalLike(this, fusionData, 1.25D, 3.0F, 1.8F, 48.0F));

        // owner assist + self-defense
        this.targetSelector.addGoal(1, new FusionBrain.OwnerHurtTargetGoalLike(this, fusionData));
        this.targetSelector.addGoal(2, new FusionBrain.OwnerHurtByTargetGoalLike(this, fusionData));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    private static void removeGoals(net.minecraft.world.entity.ai.goal.GoalSelector selector, Class<?>... types) {
        var copy = new java.util.ArrayList<>(selector.getAvailableGoals());
        for (var wrapped : copy) {
            var g = wrapped.getGoal();
            for (var t : types) if (t.isInstance(g)) selector.removeGoal(g);
        }
    }

    /* ---------------- Lifecycle ---------------- */

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        ensureFusionData();

        // apply attack override as soon as we exist
        if (fusionData.hasAttackOverride() && getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(fusionData.attackOverride());
        }

        // NOTE: removed the maybeGiveFirstSpawnXP() call here to avoid double-award
    }

    /* ---------------- Save / Load ---------------- */

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

        // Load your normal FusionData first
        fusionData.load(tag);

        // Bridge: accept summon/save NBT variations and optional "fusion" subtag
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

        // re-apply attack override after load
        if (fusionData.hasAttackOverride() && getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(fusionData.attackOverride());
        }
    }

    /* ---------------- Player Interaction ---------------- */

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return super.mobInteract(player, hand);
        ensureFusionData();

        // Claim ownership: crouch + right-click if unowned
        if (fusionData.getOwner(level()).isEmpty() && player.isShiftKeyDown()) {
            if (!level().isClientSide) {
                setFusionOwner(player);
                player.swing(hand, true); // server broadcasts swing
            }
            return InteractionResult.sidedSuccess(level().isClientSide); // client animates immediately
        }

        // Owner: empty-hand right-click toggles sit/follow
        if (isOwnedBy(player) && player.getItemInHand(hand).isEmpty()) {
            if (!level().isClientSide) {
                FusionBrain.toggleSit(this, fusionData);
                boolean nowSitting = fusionData.isSitting();

                player.swing(hand, true);
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "message.ageofminecraft.fusion.state." + (nowSitting ? "sitting" : "following"),
                                this.getDisplayName() // uses custom name if set
                        ),
                        true // action bar (bed-style popup)
                );
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    /* ---------------- OwnableFusion (set by FusionItem) ---------------- */

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

    /* ---------------- Helpers ---------------- */

    private void ensureFusionData() {
        if (this.fusionData == null) this.fusionData = new FusionBrain.FusionData();
    }

    private boolean isOwnedBy(Player p) {
        ensureFusionData();
        return fusionData.getOwner(level())
                .map(o -> o.getUUID().equals(p.getUUID()))
                .orElse(false);
    }

    /**
     * Register attributes in your EntityAttributeCreationEvent:
     * event.put(ModEntityTypes.CHICKEN_FUSION.get(), FusionChickenEntity.createAttributes().build());
     */
    public static AttributeSupplier.Builder createAttributes() {
        // Base chicken attrs + attack damage so we can override safely
        return Chicken.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            ensureFusionData();
            // one-time XP grant (covers JSON/item-provided XP)
            FusionBrain.maybeGiveFirstSpawnXP(this, fusionData);
        }
    }
}