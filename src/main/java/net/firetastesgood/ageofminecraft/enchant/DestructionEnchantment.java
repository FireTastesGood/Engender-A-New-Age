package net.firetastesgood.ageofminecraft.enchant;

import net.firetastesgood.ageofminecraft.registry.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class DestructionEnchantment extends Enchantment {
    public DestructionEnchantment(Rarity rarity, EnchantmentCategory cat, EquipmentSlot... slots) {
        super(rarity, cat, slots);
    }
    @Override public int getMinCost(int level) { return 5 + (level - 1) * 8; }
    @Override public int getMaxCost(int level) { return getMinCost(level) + 20; }
    @Override public int getMaxLevel() { return 5; }
    @Override public boolean checkCompatibility(Enchantment other) {
        return !(other instanceof DamageEnchantment)
                && other != ModEnchantments.DISRUPTION.get()
                && other != ModEnchantments.CONVICTION.get()
                && super.checkCompatibility(other);
    }
}