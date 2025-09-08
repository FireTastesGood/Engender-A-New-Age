package net.firetastesgood.ageofminecraft.client;

import net.firetastesgood.ageofminecraft.EngenderMod;
import net.firetastesgood.ageofminecraft.client.render.EntropyOrbRenderer;
import net.firetastesgood.ageofminecraft.client.render.ManaOrbRenderer;
import net.firetastesgood.ageofminecraft.registry.ModEntityTypes;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = EngenderMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {} // no instances

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Entity renderers
            EntityRenderers.register(ModEntityTypes.MANA_ORB.get(), ManaOrbRenderer::new);
            EntityRenderers.register(ModEntityTypes.ENTROPY_ORB.get(), EntropyOrbRenderer::new);

            // Item property predicates (fill_state for crystals + wellspring pulse)
            ClientItemProperties.register();
        });
    }
}