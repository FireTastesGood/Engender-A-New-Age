package net.firetastesgood.ageofminecraft.util;

import net.firetastesgood.ageofminecraft.items.EntropyCrystalItem;
import net.firetastesgood.ageofminecraft.items.InfiniteWellspringItem;
import net.firetastesgood.ageofminecraft.items.ManaCrystalItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public final class CrystalHelper {
    private CrystalHelper(){}

    public static int getManaCapacityForTier(int tier) {
        return switch (tier) {
            case 1 -> 1000;
            case 2 -> 1500;
            case 3 -> 3000;
            case 4 -> 5000;
            case 5 -> 10000;
            case 6 -> 35000;
            case 7 -> 75000;
            case 8 -> 125000;
            case 9 -> 250000;
            case 10 -> 1000000;
            default -> 1000;
        };
    }
    public static int getEntropyCapacityForTier(int tier) {
        return switch (tier) {
            case 1 -> 20;
            case 2 -> 40;
            case 3 -> 80;
            case 4 -> 150;
            case 5 -> 400;
            case 6 -> 750;
            case 7 -> 1000;
            case 8 -> 2000;
            case 9 -> 5000;
            case 10 -> 10000;
            default -> 20;
        };
    }

    public static final Rarity ARTIFACT_RARITY = Rarity.create("ARTIFACT", (c) -> c.withColor(0xFFD700));

    /** First mana crystal with space, or wellsprint item (always space). Returns stack index or -1. */
    public static int findManaReceiverSlot(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.isEmpty()) continue;
            if (s.getItem() instanceof InfiniteWellspringItem) return i;
            if (s.getItem() instanceof ManaCrystalItem mc) {
                if (mc.getCurrent(s) < mc.getMax()) return i;
            }
        }
        return -1;
    }

    /** First entropy crystal with space, or wellspring. */
    public static int findEntropyReceiverSlot(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.isEmpty()) continue;
            if (s.getItem() instanceof InfiniteWellspringItem) return i;
            if (s.getItem() instanceof EntropyCrystalItem ec) {
                if (ec.getCurrent(s) < ec.getMax()) return i;
            }
        }
        return -1;
    }

    public static int depositMana(ItemStack stack, int amount) {
        if (stack.getItem() instanceof InfiniteWellspringItem ws) {
            ws.addMana(stack, amount);
            return amount;
        }
        if (stack.getItem() instanceof ManaCrystalItem mc) {
            int cur = mc.getCurrent(stack);
            int max = mc.getMax();
            int can = Math.max(0, max - cur);
            int inc = Math.min(can, amount);
            if (inc > 0) mc.addMana(stack, inc);
            return inc;
        }
        return 0;
    }

    public static int depositEntropy(ItemStack stack, int amount) {
        if (stack.getItem() instanceof InfiniteWellspringItem ws) {
            ws.addEntropy(stack, amount);
            return amount;
        }
        if (stack.getItem() instanceof EntropyCrystalItem ec) {
            int cur = ec.getCurrent(stack);
            int max = ec.getMax();
            int can = Math.max(0, max - cur);
            int inc = Math.min(can, amount);
            if (inc > 0) ec.addEntropy(stack, inc);
            return inc;
        }
        return 0;
    }
}