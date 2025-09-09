package net.firetastesgood.ageofminecraft.items;

import net.firetastesgood.ageofminecraft.util.CrystalHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class EntropyCrystalItem extends Item {
    private final int tier;

    public EntropyCrystalItem(int tier, Properties props) {
        super(props);
        this.tier = tier;
    }

    public int getTier() { return tier; }

    public int getCurrent(ItemStack stack) {
        return stack.getOrCreateTag().getInt("entropy");
    }

    public int getMax() {
        return CrystalHelper.getEntropyCapacityForTier(tier);
    }

    public void addEntropy(ItemStack stack, int delta) {
        int cur = getCurrent(stack);
        int max = getMax();
        int next = Math.min(max, Math.max(0, cur + delta));
        stack.getOrCreateTag().putInt("entropy", next);
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public Rarity getRarity(ItemStack stack) { return Rarity.EPIC; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flags) {
        int cur = getCurrent(stack);
        int max = getMax();
        tip.add(Component.translatable("item.ageofminecraft.crystal.entropy.desc")
                .withStyle(ChatFormatting.GRAY));
        tip.add(Component.literal("Entropy Count: ")
                .append(Component.literal(String.valueOf(cur)).withStyle(ChatFormatting.DARK_RED))
                .append(Component.literal(" / "))
                .append(Component.literal(String.valueOf(max)).withStyle(ChatFormatting.DARK_RED)));
    }
}