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

public class InfiniteWellspringItem extends Item {
    public InfiniteWellspringItem(Properties props) {
        super(props);
    }

    public int getMana(ItemStack stack) { return stack.getOrCreateTag().getInt("mana"); }
    public int getEntropy(ItemStack stack) { return stack.getOrCreateTag().getInt("entropy"); }
    public void addMana(ItemStack s, int d) { s.getOrCreateTag().putInt("mana", Math.max(0, getMana(s)+d)); }
    public void addEntropy(ItemStack s, int d){ s.getOrCreateTag().putInt("entropy", Math.max(0, getEntropy(s)+d)); }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    // Golden rarity
    @Override public Rarity getRarity(ItemStack stack) { return CrystalHelper.ARTIFACT_RARITY; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flags) {
        tip.add(Component.literal("(ARTIFACT)").withStyle(ChatFormatting.GOLD));
        tip.add(Component.translatable("item.ageofminecraft.infinite_wellspring.desc").withStyle(ChatFormatting.GRAY));

        int m = getMana(stack);
        int e = getEntropy(stack);
        if (m > 0)
            tip.add(Component.literal("Mana Count: ").append(Component.literal(String.valueOf(m)).withStyle(ChatFormatting.AQUA)));
        if (e > 0)
            tip.add(Component.literal("Entropy Count: ").append(Component.literal(String.valueOf(e)).withStyle(ChatFormatting.DARK_RED)));
    }
}