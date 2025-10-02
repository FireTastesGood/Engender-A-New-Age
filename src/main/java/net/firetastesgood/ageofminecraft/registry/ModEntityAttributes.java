package net.firetastesgood.ageofminecraft.registry;

import net.firetastesgood.ageofminecraft.EngenderMod;
import net.firetastesgood.ageofminecraft.entity.fusion.FusionChickenEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EngenderMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntityAttributes {
    private ModEntityAttributes() {}

    @SubscribeEvent
    public static void onEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.CHICKEN_FUSION.get(), FusionChickenEntity.createAttributes().build());
    }
}