package net.firetastesgood.ageofminecraft.registry;

import net.firetastesgood.ageofminecraft.EngenderMod;
import net.firetastesgood.ageofminecraft.fusion.FusionPartItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EngenderMod.MODID);

    public static final RegistryObject<CreativeModeTab> ENGENDER_TAB =
            TABS.register("engender_tab", () -> CreativeModeTab.builder()
                    .title(Component.literal("Engender Mod"))
                    .icon(() -> new ItemStack(ModItems.MANA_CRYSTAL_1.get()))
                    .displayItems((params, out) -> {
                        out.accept(ModItems.MANA_CRYSTAL_1.get());
                        out.accept(ModItems.ENTROPY_CRYSTAL_1.get());
                        out.accept(ModItems.INFINITE_WELLSPRING.get());
                        out.accept(ModBlocks.FUSION_CRAFTER.get());
                        out.accept(ModItems.WOODEN_CLEAVER.get());
                        out.accept(ModItems.STONE_CLEAVER.get());
                        out.accept(ModItems.IRON_CLEAVER.get());
                        out.accept(ModItems.GOLDEN_CLEAVER.get());
                        out.accept(ModItems.DIAMOND_CLEAVER.get());
                        out.accept(ModItems.NETHERITE_CLEAVER.get());

                        out.accept(FusionPartItem.withData(
                                new ItemStack(ModItems.FUSION_PART_CHICKEN.get()),
                                new ResourceLocation(EngenderMod.MODID, "chicken_fusion"),
                                1,
                                1,
                                0
                        ));

                        out.accept(ModItems.CHICKEN_FUSION.get());
                        out.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.CRUSHER.get(), 5)));
                        out.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.DISRUPTION.get(), 5)));
                        out.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.CONVICTION.get(), 5)));
                        out.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.OBLITERATION.get(), 1)));
                        out.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.NEGLECTION.get(), 1)));
                        out.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ModEnchantments.SUPERWEAPON.get(), 1)));
                    })
                    .build());
}