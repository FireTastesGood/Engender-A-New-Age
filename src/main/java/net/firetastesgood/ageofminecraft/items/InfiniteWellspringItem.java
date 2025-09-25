package net.firetastesgood.ageofminecraft.items;

import net.firetastesgood.ageofminecraft.util.CrystalHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class InfiniteWellspringItem extends Item {
    public static final String NBT_MANA    = "Mana";
    public static final String NBT_ENTROPY = "Entropy";

    public static final int DEFAULT_MANA    = 0;
    public static final int DEFAULT_ENTROPY = 0;

    public InfiniteWellspringItem(Properties props) {
        super(props);
    }

    private static CompoundTag ensureInit(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_MANA))    tag.putInt(NBT_MANA,    DEFAULT_MANA);
        if (!tag.contains(NBT_ENTROPY)) tag.putInt(NBT_ENTROPY, DEFAULT_ENTROPY);
        return tag;
    }

    public static int getMana(ItemStack stack) {
        return ensureInit(stack).getInt(NBT_MANA);
    }

    public static int getEntropy(ItemStack stack) {
        return ensureInit(stack).getInt(NBT_ENTROPY);
    }

    public static void setMana(ItemStack stack, int value) {
        ensureInit(stack).putInt(NBT_MANA, Math.max(0, value));
    }

    public static void setEntropy(ItemStack stack, int value) {
        ensureInit(stack).putInt(NBT_ENTROPY, Math.max(0, value));
    }

    public static void addMana(ItemStack stack, int d) {
        setMana(stack, getMana(stack) + d);
    }

    public static void addEntropy(ItemStack stack, int d) {
        setEntropy(stack, getEntropy(stack) + d);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        ensureInit(stack);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack s = super.getDefaultInstance();
        ensureInit(s);
        return s;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        if (!level.isClientSide) ensureInit(stack);
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override public Rarity getRarity(ItemStack stack) { return CrystalHelper.ARTIFACT_RARITY; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tip, TooltipFlag flags) {
        ensureInit(stack);
        tip.add(Component.translatable("item.ageofminecraft.infinite_wellspring.desc").withStyle(ChatFormatting.GRAY));

        int m = getMana(stack);
        int e = getEntropy(stack);

        tip.add(
                Component.literal("Mana : ")
                        .append(Component.literal(String.valueOf(m)))
                        .withStyle(ChatFormatting.AQUA)
        );

        tip.add(
                Component.literal("Entropy : ")
                        .append(Component.literal(String.valueOf(e)))
                        .withStyle(ChatFormatting.DARK_RED)
        );
    }
}
