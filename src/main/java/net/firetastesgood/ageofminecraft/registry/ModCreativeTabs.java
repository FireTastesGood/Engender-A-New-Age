package net.firetastesgood.ageofminecraft.registry;

import net.firetastesgood.ageofminecraft.EngenderMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, EngenderMod.MODID);

    public static final RegistryObject<CreativeModeTab> ENGENDER_TAB =
            TABS.register("engender_tab", () -> CreativeModeTab.builder()
                    .title(Component.literal("Engender Mod"))
                    .icon(() -> new ItemStack(ModItems.MANA_CRYSTAL_1.get()))
                    .displayItems((params, out) -> {
                        out.accept(ModItems.MANA_CRYSTAL_1.get());
                        out.accept(ModItems.ENTROPY_CRYSTAL_1.get());
                        out.accept(ModItems.INFINITE_WELLSPRING.get());
                    })
                    .build());
}