package net.firetastesgood.ageofminecraft.registry;

import net.firetastesgood.ageofminecraft.fusion.FusionCrafterBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "ageofminecraft");

    public static final RegistryObject<BlockEntityType<FusionCrafterBlockEntity>> FUSION_CRAFTER =
            BLOCK_ENTITIES.register("fusion_crafter",
                    () -> BlockEntityType.Builder.of(FusionCrafterBlockEntity::new,
                            ModBlocks.FUSION_CRAFTER.get()).build(null));
}