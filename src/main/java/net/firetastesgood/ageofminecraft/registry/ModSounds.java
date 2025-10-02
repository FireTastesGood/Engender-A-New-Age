package net.firetastesgood.ageofminecraft.registry;

import net.firetastesgood.ageofminecraft.EngenderMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, EngenderMod.MODID);

    public static final RegistryObject<SoundEvent> SLASH_FLESH =
            SOUND_EVENTS.register("slashflesh",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(EngenderMod.MODID, "slashflesh")));

    public static final RegistryObject<SoundEvent> FUSION_SPAWN =
            SOUND_EVENTS.register("fusion_spawn",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(EngenderMod.MODID, "fusion_spawn")));
}