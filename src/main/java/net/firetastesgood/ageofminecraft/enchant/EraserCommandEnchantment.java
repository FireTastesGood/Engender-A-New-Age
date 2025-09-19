package net.firetastesgood.ageofminecraft.enchant;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import java.util.Set;

public class EraserCommandEnchantment extends Enchantment {
    public EraserCommandEnchantment(Rarity rarity, EnchantmentCategory cat, EquipmentSlot... slots) {
        super(rarity, cat, slots);
    }

    @Override public int getMinCost(int level) { return 50; }
    @Override public int getMaxCost(int level) { return 100; }
    @Override public int getMaxLevel() { return 1; }
    @Override public boolean isTreasureOnly() { return true; }

    private static final Set<ToolAction> ALLOWED_ACTIONS = Set.of(
            ToolActions.SWORD_DIG,
            ToolActions.AXE_DIG,
            ToolActions.PICKAXE_DIG,
            ToolActions.SHOVEL_DIG,
            ToolActions.HOE_DIG
    );

    private static boolean isAllowedTool(ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof TieredItem || item instanceof SwordItem) return true;

        for (ToolAction a : ALLOWED_ACTIONS) {
            if (stack.canPerformAction(a)) return true;
        }
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return isAllowedTool(stack);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return isAllowedTool(stack);
    }
}