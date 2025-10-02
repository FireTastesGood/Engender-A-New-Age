package net.firetastesgood.ageofminecraft.enchant;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class ObliterationEnchantment extends Enchantment {
    public ObliterationEnchantment(Rarity rarity, EnchantmentCategory cat, EquipmentSlot... slots) {
        super(rarity, cat, slots);
    }

    @Override public int getMinCost(int level) { return 20; }
    @Override public int getMaxCost(int level) { return getMinCost(level) + 30; }
    @Override public int getMaxLevel() { return 1; }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return super.canEnchant(stack) || stack.getItem() instanceof AxeItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return super.canApplyAtEnchantingTable(stack) || stack.getItem() instanceof AxeItem;
    }
}