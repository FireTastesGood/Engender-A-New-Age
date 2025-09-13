package net.firetastesgood.ageofminecraft.registry;

import net.firetastesgood.ageofminecraft.EngenderMod;
import net.firetastesgood.ageofminecraft.fusion.FusionCrafterMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    private ModMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, EngenderMod.MODID);

    public static final RegistryObject<MenuType<FusionCrafterMenu>> FUSION_CRAFTER =
            MENUS.register("fusion_crafter",
                    () -> IForgeMenuType.create((windowId, inv, buf) ->
                            new FusionCrafterMenu(windowId, inv, buf)
                    )
            );
}