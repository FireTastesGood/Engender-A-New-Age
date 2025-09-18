package net.firetastesgood.ageofminecraft.events;

import net.firetastesgood.ageofminecraft.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = "ageofminecraft")
public class CleaverEvents {

    private static boolean isCleaver(ItemStack stack) {
        return stack.getItem() instanceof SwordItem
                && (stack.is(ModItems.WOODEN_CLEAVER.get())
                || stack.is(ModItems.STONE_CLEAVER.get())
                || stack.is(ModItems.IRON_CLEAVER.get())
                || stack.is(ModItems.GOLDEN_CLEAVER.get())
                || stack.is(ModItems.DIAMOND_CLEAVER.get())
                || stack.is(ModItems.NETHERITE_CLEAVER.get()));
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent e) {
        LivingEntity victim = e.getEntity();
        if (!(e.getSource().getEntity() instanceof Player player)) return;

        ItemStack held = player.getMainHandItem();
        if (!isCleaver(held)) return;

        if (victim instanceof Skeleton && !(victim instanceof Stray)) {
            e.getDrops().add(victim.spawnAtLocation(new ItemStack(Items.SKELETON_SKULL)));
        } else if (victim instanceof WitherSkeleton) {
            e.getDrops().add(victim.spawnAtLocation(new ItemStack(Items.WITHER_SKELETON_SKULL)));
        } else if (victim instanceof Zombie) {
            e.getDrops().add(victim.spawnAtLocation(new ItemStack(Items.ZOMBIE_HEAD)));
        } else if (victim instanceof Creeper) {
            e.getDrops().add(victim.spawnAtLocation(new ItemStack(Items.CREEPER_HEAD)));
        } else if (victim instanceof Player) {
            e.getDrops().add(victim.spawnAtLocation(new ItemStack(Items.PLAYER_HEAD)));
        } else if (victim instanceof WitherBoss) {
            e.getDrops().add(victim.spawnAtLocation(new ItemStack(Items.WITHER_SKELETON_SKULL)));
            e.getDrops().add(victim.spawnAtLocation(new ItemStack(Items.WITHER_SKELETON_SKULL)));
            e.getDrops().add(victim.spawnAtLocation(new ItemStack(Items.WITHER_SKELETON_SKULL)));
        }
    }
}