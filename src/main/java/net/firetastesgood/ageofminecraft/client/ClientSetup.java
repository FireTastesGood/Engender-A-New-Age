package net.firetastesgood.ageofminecraft.client;

import net.firetastesgood.ageofminecraft.EngenderMod;
import net.firetastesgood.ageofminecraft.client.render.EntropyOrbRenderer;
import net.firetastesgood.ageofminecraft.client.render.ManaOrbRenderer;
import net.firetastesgood.ageofminecraft.fusion.FusionCrafterScreen;
import net.firetastesgood.ageofminecraft.registry.ModBlocks;
import net.firetastesgood.ageofminecraft.registry.ModEntityTypes;
import net.firetastesgood.ageofminecraft.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.entity.ChickenRenderer;

@Mod.EventBusSubscriber(modid = EngenderMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntityTypes.MANA_ORB.get(), ManaOrbRenderer::new);
            EntityRenderers.register(ModEntityTypes.ENTROPY_ORB.get(), EntropyOrbRenderer::new);

            ClientItemProperties.register();

            MenuScreens.register(ModMenus.FUSION_CRAFTER.get(), FusionCrafterScreen::new);

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FUSION_CRAFTER.get(), RenderType.cutout());

            EntityRenderers.register(ModEntityTypes.CHICKEN_FUSION.get(), ChickenRenderer::new);
        });
    }
}