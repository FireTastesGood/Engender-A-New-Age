package net.firetastesgood.ageofminecraft.registry;

import net.firetastesgood.ageofminecraft.EngenderMod;
import net.firetastesgood.ageofminecraft.entity.EntropyOrbEntity;
import net.firetastesgood.ageofminecraft.entity.ManaOrbEntity;
import net.firetastesgood.ageofminecraft.entity.fusion.FusionChickenEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EngenderMod.MODID);

    public static final RegistryObject<EntityType<ManaOrbEntity>> MANA_ORB =
            ENTITIES.register("mana_orb", () ->
                    EntityType.Builder.<ManaOrbEntity>of(ManaOrbEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build(new ResourceLocation(EngenderMod.MODID, "mana_orb").toString()));

    public static final RegistryObject<EntityType<EntropyOrbEntity>> ENTROPY_ORB =
            ENTITIES.register("entropy_orb", () ->
                    EntityType.Builder.<EntropyOrbEntity>of(EntropyOrbEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build(new ResourceLocation(EngenderMod.MODID, "entropy_orb").toString()));

    public static final RegistryObject<EntityType<FusionChickenEntity>> CHICKEN_FUSION =
            ENTITIES.register("chicken_fusion", () ->
                    EntityType.Builder.<FusionChickenEntity>of(FusionChickenEntity::new, MobCategory.CREATURE)
                            .sized(0.4F, 0.7F).build(new ResourceLocation(EngenderMod.MODID, "chicken_fusion").toString()));

    private ModEntityTypes() {}
}