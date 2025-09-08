package net.firetastesgood.ageofminecraft.client;

import net.firetastesgood.ageofminecraft.EngenderMod;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.firetastesgood.ageofminecraft.items.EntropyCrystalItem;
import net.firetastesgood.ageofminecraft.items.InfiniteWellspringItem;
import net.firetastesgood.ageofminecraft.items.ManaCrystalItem;
import net.firetastesgood.ageofminecraft.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public final class ClientItemProperties {
    private ClientItemProperties(){}

    private static final ResourceLocation FILL_STATE = new ResourceLocation(EngenderMod.MODID, "fill_state");

    public static void register() {
        // Mana: state 0..3
        registerFamily(() -> ModItems.MANA_CRYSTAL_1.get());
        registerFamily(() -> ModItems.MANA_CRYSTAL_2.get());
        registerFamily(() -> ModItems.MANA_CRYSTAL_3.get());
        registerFamily(() -> ModItems.MANA_CRYSTAL_4.get());
        registerFamily(() -> ModItems.MANA_CRYSTAL_5.get());
        registerFamily(() -> ModItems.MANA_CRYSTAL_6.get());
        registerFamily(() -> ModItems.MANA_CRYSTAL_7.get());
        registerFamily(() -> ModItems.MANA_CRYSTAL_8.get());
        registerFamily(() -> ModItems.MANA_CRYSTAL_9.get());
        registerFamily(() -> ModItems.MANA_CRYSTAL_10.get());

        // Entropy: state 0..3
        registerFamily(() -> ModItems.ENTROPY_CRYSTAL_1.get());
        registerFamily(() -> ModItems.ENTROPY_CRYSTAL_2.get());
        registerFamily(() -> ModItems.ENTROPY_CRYSTAL_3.get());
        registerFamily(() -> ModItems.ENTROPY_CRYSTAL_4.get());
        registerFamily(() -> ModItems.ENTROPY_CRYSTAL_5.get());
        registerFamily(() -> ModItems.ENTROPY_CRYSTAL_6.get());
        registerFamily(() -> ModItems.ENTROPY_CRYSTAL_7.get());
        registerFamily(() -> ModItems.ENTROPY_CRYSTAL_8.get());
        registerFamily(() -> ModItems.ENTROPY_CRYSTAL_9.get());
        registerFamily(() -> ModItems.ENTROPY_CRYSTAL_10.get());

        // ClientItemProperties.register(...)
        ItemProperties.register(ModItems.INFINITE_WELLSPRING.get(),
                new ResourceLocation("ageofminecraft", "fill_state"),
                (stack, level, entity, seed) -> {
                    // Safe tick source (works even in menus)
                    long ticks = (level != null ? level.getGameTime() : (entity != null ? entity.tickCount : 0));
                    float phase = (ticks % 10) / 10.0f;
                    return phase;
                });
    }

    private static void registerFamily(Supplier<Item> item) {
        ItemProperties.register(item.get(), FILL_STATE,
                (ItemStack stack, ClientLevel level, LivingEntity entity, int seed) -> {
                    if (stack.getItem() instanceof ManaCrystalItem mc) {
                        int cur = mc.getCurrent(stack);
                        int max = mc.getMax();
                        if (max <= 0 || cur <= 0) return 0;
                        if (cur >= max) return 3;
                        float pct = cur / (float) max;
                        return pct >= 0.5f ? 2 : 1;
                    }
                    if (stack.getItem() instanceof EntropyCrystalItem ec) {
                        int cur = ec.getCurrent(stack);
                        int max = ec.getMax();
                        if (max <= 0 || cur <= 0) return 0;
                        if (cur >= max) return 3;
                        float pct = cur / (float) max;
                        return pct >= 0.5f ? 2 : 1;
                    }
                    return 0;
                });
    }
}