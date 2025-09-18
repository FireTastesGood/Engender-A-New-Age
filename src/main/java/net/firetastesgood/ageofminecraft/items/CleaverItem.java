package net.firetastesgood.ageofminecraft.items;

import net.firetastesgood.ageofminecraft.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CleaverItem extends SwordItem {

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Right click to butcher animals").withStyle(ChatFormatting.GOLD));
    }

    private final int displayDamageForLoot;

    public CleaverItem(Tier tier, int damageModifier, float attackSpeedModifier, Properties props) {
        super(tier, damageModifier, attackSpeedModifier, props);

        int tierBonus = Math.round(tier.getAttackDamageBonus());
        this.displayDamageForLoot = damageModifier + tierBonus + 1;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide) {
            attacker.level().playSound(
                    null,
                    target.blockPosition(),
                    ModSounds.SLASH_FLESH.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    attacker.getRandom().nextFloat() * 0.2F + 0.9F
            );
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();
        if (level.isClientSide) return InteractionResult.PASS;

        if (!(target instanceof Animal animal)) return InteractionResult.PASS;
        if (animal.isBaby()) return InteractionResult.PASS;
        if (animal.isInLove()) return InteractionResult.PASS;

        int looting = EnchantmentHelper.getMobLooting(player);
        int extraRolls = this.displayDamageForLoot + (3 * looting) + 3;

        player.level().playSound(
                null,
                target.blockPosition(),
                ModSounds.SLASH_FLESH.get(),
                SoundSource.PLAYERS,
                1.0F,
                player.getRandom().nextFloat() * 0.2F + 0.9F
        );

        player.swing(hand, true);
        DamageSource src = level.damageSources().playerAttack(player);
        target.hurt(src, Float.MAX_VALUE);

        if (!target.isAlive() && target instanceof Mob mob) {
            ServerLevel sLevel = (ServerLevel) level;

            LootTable table = sLevel.getServer().getLootData().getLootTable(mob.getLootTable());
            LootParams.Builder ctxBuilder = new LootParams.Builder(sLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, mob)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(mob.blockPosition()))
                    .withParameter(LootContextParams.DAMAGE_SOURCE, src)
                    .withOptionalParameter(LootContextParams.KILLER_ENTITY, player)
                    .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, player)
                    .withLuck(player.getLuck());

            LootParams params = ctxBuilder.create(LootContextParamSets.ENTITY);

            for (int i = 0; i < extraRolls; i++) {
                List<ItemStack> stacks = table.getRandomItems(params);
                for (ItemStack is : stacks) {
                    mob.spawnAtLocation(is.copy());
                }
            }
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        }
        return InteractionResult.SUCCESS;
    }
}