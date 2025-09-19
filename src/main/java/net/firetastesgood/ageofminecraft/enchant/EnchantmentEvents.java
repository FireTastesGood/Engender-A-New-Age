package net.firetastesgood.ageofminecraft.enchant;

import net.firetastesgood.ageofminecraft.EngenderMod;
import net.firetastesgood.ageofminecraft.registry.ModEnchantments;
import net.firetastesgood.ageofminecraft.registry.ModTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EngenderMod.MODID)
public final class EnchantmentEvents {

    private static final java.util.Map<java.util.UUID, Long> OBLIT_SAFE =
            new java.util.concurrent.ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onDetonate(net.minecraftforge.event.level.ExplosionEvent.Detonate event) {
        long now = event.getLevel().getGameTime();
        event.getAffectedEntities().removeIf(ent -> {
            if (!(ent instanceof net.minecraft.world.entity.LivingEntity liv)) return false;
            Long t = OBLIT_SAFE.get(liv.getUUID());
            boolean protect = (t != null && now - t <= 2);
            if (protect) OBLIT_SAFE.remove(liv.getUUID());
            return protect;
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        LivingEntity target = event.getEntity();
        ItemStack main = attacker.getMainHandItem();
        if (main.isEmpty()) return;

        float bonus = 0.0F;

        int lvl = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DISRUPTION.get(), main);
        if (lvl > 0 && target.getType().is(ModTags.EntityTypes.END_RESIDENTS)) {
            bonus += 3.5F * lvl;
        }

        lvl = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CONVICTION.get(), main);
        if (lvl > 0 && target.getType().is(ModTags.EntityTypes.ILLAGER_RAID_MOBS)) {
            bonus += 3.0F * lvl;
        }

        lvl = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.CRUSHER.get(), main);
        if (lvl > 0) {
            if (target.getType().is(ModTags.EntityTypes.GOLEMS)) bonus += 10.0F * lvl;
            else bonus += 0.5F * lvl;
        }

        if (bonus > 0.0F) {
            event.setAmount(event.getAmount() + bonus);
        }

        lvl = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NEGLECTION.get(), main);
        if (lvl > 0 && target.invulnerableTime > 0) {
            target.invulnerableTime = 0;
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        net.minecraft.world.entity.LivingEntity target = event.getEntity();
        net.minecraft.world.damagesource.DamageSource source = event.getSource();

        if (source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) return;

        net.minecraft.world.entity.Entity atkEnt = source.getEntity();
        if (!(atkEnt instanceof net.minecraft.world.entity.LivingEntity attacker)) return;
        if (source.getDirectEntity() != attacker) return;

        int lvl = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                net.firetastesgood.ageofminecraft.registry.ModEnchantments.OBLITERATION.get(),
                attacker.getMainHandItem());
        if (lvl <= 0) return;

        if (!( !(target instanceof net.minecraft.world.entity.boss.wither.WitherBoss)
                && !(target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon))) return;

        net.minecraft.world.level.Level level = target.level();
        level.broadcastEntityEvent(target, (byte)20);

        final float basePower = (float)(target.getBbHeight() + target.getBbWidth());

        final double cx = target.getX();
        final double cy = target.getY();
        final double cz = target.getZ();

        OBLIT_SAFE.put(attacker.getUUID(), level.getGameTime());

        level.explode(
                null,
                cx, cy, cz,
                basePower,
                net.minecraft.world.level.Level.ExplosionInteraction.NONE
        );

        final float tntPower = 4.0F;
        final float extra = Math.max(0.0F, tntPower - basePower);
        if (extra > 0.0F) {
            final double radius = tntPower * 2.0D + 1.0D;

            java.util.List<net.minecraft.world.entity.LivingEntity> victims =
                    level.getEntitiesOfClass(
                            net.minecraft.world.entity.LivingEntity.class,
                            new net.minecraft.world.phys.AABB(
                                    cx - radius, cy - radius, cz - radius,
                                    cx + radius, cy + radius, cz + radius),
                            e -> e.isAlive()
                    );

            for (net.minecraft.world.entity.LivingEntity e : victims) {
                if (e == attacker) continue;

                double dx = e.getX() - cx;
                double dy = e.getY() - cy;
                double dz = e.getZ() - cz;
                double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
                if (dist < 1.0E-4) continue;

                dx /= dist; dy /= dist; dz /= dist;

                double push = 0.8D * extra;
                double yBoost = 0.10D + 0.05D * extra;

                e.push(dx * push, yBoost, dz * push);
            }
        }

        target.discard();
    }

    private static boolean isNonBoss(LivingEntity entity) {
        return !(entity instanceof WitherBoss) && !(entity instanceof EnderDragon);
    }

    private EnchantmentEvents() {}

    @SubscribeEvent
    public static void onItemAttributeModifiers(net.minecraftforge.event.ItemAttributeModifierEvent event) {
        if (event.getSlotType() != net.minecraft.world.entity.EquipmentSlot.MAINHAND) return;

        final net.minecraft.world.item.ItemStack stack = event.getItemStack();
        final int lvl = net.minecraft.world.item.enchantment.EnchantmentHelper
                .getItemEnchantmentLevel(net.firetastesgood.ageofminecraft.registry.ModEnchantments.SUPERWEAPON.get(), stack);
        if (lvl <= 0) return;

        final net.minecraft.world.entity.ai.attributes.Attribute attackAttr =
                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE;

        final com.google.common.collect.Multimap<
                net.minecraft.world.entity.ai.attributes.Attribute,
                net.minecraft.world.entity.ai.attributes.AttributeModifier
                > baseMap = stack.getItem().getDefaultAttributeModifiers(event.getSlotType());

        java.util.UUID baseUuid = null;
        String baseName = "Weapon modifier";
        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation baseOp =
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;
        double baseAmt = 0.0D;

        final java.util.Collection<net.minecraft.world.entity.ai.attributes.AttributeModifier> baseCol = baseMap.get(attackAttr);
        if (baseCol != null && !baseCol.isEmpty()) {
            final net.minecraft.world.entity.ai.attributes.AttributeModifier m = baseCol.iterator().next();
            baseUuid = m.getId();
            baseName = m.getName();
            baseOp   = m.getOperation();
            baseAmt  = m.getAmount();
        }

        final java.util.Collection<net.minecraft.world.entity.ai.attributes.AttributeModifier> currentCol =
                event.getModifiers().get(attackAttr);

        if (baseUuid != null) {
            if (currentCol != null && !currentCol.isEmpty()) {
                for (net.minecraft.world.entity.ai.attributes.AttributeModifier mod :
                        new java.util.ArrayList<>(currentCol)) {
                    if (mod.getId().equals(baseUuid)) {
                        event.removeModifier(attackAttr, mod);
                        break;
                    }
                }
            }
            final net.minecraft.world.entity.ai.attributes.AttributeModifier replaced =
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                            baseUuid, baseName, baseAmt + 50.0D, baseOp);
            event.addModifier(attackAttr, replaced);
        } else {
            final java.util.UUID uuid = java.util.UUID.fromString("4a1c7a2b-5f33-4a6a-9d7b-2c2a0d9c1d11");
            final net.minecraft.world.entity.ai.attributes.AttributeModifier add =
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                            uuid, "The Eraser Command", 50.0D,
                            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION);
            event.addModifier(attackAttr, add);
        }
    }
}