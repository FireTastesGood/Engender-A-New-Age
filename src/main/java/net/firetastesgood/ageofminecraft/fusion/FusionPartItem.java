package net.firetastesgood.ageofminecraft.fusion;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class FusionPartItem extends Item {
    public static final String NBT_MOB_ID        = "MobId";
    public static final String NBT_TIER          = "Tier";
    public static final String NBT_MANA_COST     = "ManaCost";
    public static final String NBT_ENTROPY_COST  = "EntropyCost";

    public FusionPartItem(Properties props) {
        super(props);
    }

    public static ItemStack withData(ItemStack stack, ResourceLocation mobId, int tier, int mana, int entropy) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(NBT_MOB_ID, mobId.toString());
        tag.putInt(NBT_TIER, tier);
        tag.putInt(NBT_MANA_COST, mana);
        tag.putInt(NBT_ENTROPY_COST, entropy);
        return stack;
    }

    public static ResourceLocation getMobId(ItemStack stack) {
        CompoundTag t = stack.getTag();
        return (t != null && t.contains(NBT_MOB_ID)) ? ResourceLocation.tryParse(t.getString(NBT_MOB_ID)) : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag t = stack.getTag();
        if (t == null) return;

        if (t.contains(NBT_MANA_COST)) {
            int mana = t.getInt(NBT_MANA_COST);
            tooltip.add(Component.literal("Mana Cost: " + mana).withStyle(ChatFormatting.AQUA));
        }

        if (t.contains(NBT_ENTROPY_COST)) {
            int ent = t.getInt(NBT_ENTROPY_COST);
            if (ent > 0) {
                tooltip.add(Component.literal("Entropy Cost: " + ent).withStyle(ChatFormatting.DARK_RED));
            }
        }
    }
}