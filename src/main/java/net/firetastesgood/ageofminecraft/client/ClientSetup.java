package net.firetastesgood.ageofminecraft.client;

import net.firetastesgood.ageofminecraft.client.render.EntropyOrbRenderer;
import net.firetastesgood.ageofminecraft.client.render.ManaOrbRenderer;
import net.firetastesgood.ageofminecraft.registry.ModEntityTypes;
import net.minecraft.client.renderer.entity.ExperienceOrbRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.firetastesgood.ageofminecraft.EngenderMod;

@Mod.EventBusSubscriber(modid = EngenderMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        EntityRenderers.register(ModEntityTypes.MANA_ORB.get(), ctx -> new ManaOrbRenderer(ctx));
        EntityRenderers.register(ModEntityTypes.ENTROPY_ORB.get(), ctx -> new EntropyOrbRenderer(ctx));
    }
}