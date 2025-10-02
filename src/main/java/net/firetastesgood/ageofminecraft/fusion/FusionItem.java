package net.firetastesgood.ageofminecraft.fusion;

import net.firetastesgood.ageofminecraft.entity.fusion.OwnableFusion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import java.util.function.Supplier;

public class FusionItem extends Item {
    public static final String NBT_MOB_ID          = "MobId";
    public static final String NBT_FIRST_SPAWN_XP  = "FirstSpawnXP";
    public static final String NBT_ATTACK_OVERRIDE = "AttackDamageOverride";

    private final Supplier<EntityType<? extends Mob>> fusionEntityType;

    public FusionItem(Properties props, Supplier<EntityType<? extends Mob>> type) {
        super(props);
        this.fusionEntityType = type;
    }

    public static ItemStack withData(ItemStack stack, ResourceLocation mobId, int firstSpawnXp, double attackOverride) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(NBT_MOB_ID, mobId.toString());
        tag.putInt(NBT_FIRST_SPAWN_XP, firstSpawnXp);
        tag.putDouble(NBT_ATTACK_OVERRIDE, attackOverride);
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        var level  = ctx.getLevel();
        var player = ctx.getPlayer();
        var stack  = ctx.getItemInHand();
        if (player == null) return InteractionResult.PASS;

        if (level.isClientSide) return InteractionResult.sidedSuccess(true);

        ServerLevel server = (ServerLevel) level;

        Direction face = ctx.getClickedFace();
        BlockPos base  = ctx.getClickedPos();
        BlockPos pos   = base.relative(face == Direction.DOWN ? Direction.UP : face);

        CompoundTag root = stack.getTag();
        CompoundTag data = (root != null && root.contains("fusion", 10)) ? root.getCompound("fusion") : root;

        int firstSpawnXp = 0;
        double attackOverride = -1.0;

        if (data != null) {
            if (data.contains(NBT_FIRST_SPAWN_XP)) firstSpawnXp = data.getInt(NBT_FIRST_SPAWN_XP);
            else if (data.contains("FirstSpawnXp")) firstSpawnXp = data.getInt("FirstSpawnXp");
            else if (data.contains("first_spawn_xp")) firstSpawnXp = data.getInt("first_spawn_xp");

            if (data.contains(NBT_ATTACK_OVERRIDE)) attackOverride = data.getDouble(NBT_ATTACK_OVERRIDE);
            else if (data.contains("attack_damage_override")) attackOverride = data.getDouble("attack_damage_override");
        }

        EntityType<? extends Mob> type = fusionEntityType.get();
        Mob mob = type.create(server);
        if (mob == null) return InteractionResult.FAIL;

        mob.moveTo(pos.getX() + 0.5, pos.getY() + 0.01, pos.getZ() + 0.5, player.getYRot(), 0.0F);

        if (mob instanceof OwnableFusion fusion) {
            fusion.setFusionOwner(player);
            fusion.setFusionFirstSpawnXP(firstSpawnXp);
            if (attackOverride >= 0.0) fusion.setFusionAttackOverride(attackOverride);
        }

        server.addFreshEntity(mob);

        if (firstSpawnXp > 0) {
            ExperienceOrb.award(server, mob.position(), firstSpawnXp);
            if (mob instanceof OwnableFusion fusion) {
                fusion.setFusionFirstSpawnXP(0);
            }
        }

        if (!player.getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.sidedSuccess(false);
    }
}