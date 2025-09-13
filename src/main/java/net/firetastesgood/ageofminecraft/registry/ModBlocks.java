package net.firetastesgood.ageofminecraft.registry;

import net.firetastesgood.ageofminecraft.fusion.FusionCrafterBlock;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, "ageofminecraft");

    public static final RegistryObject<Block> FUSION_CRAFTER = BLOCKS.register(
            "fusion_crafter",
            () -> new FusionCrafterBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(3.5F, 3.5F)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .requiresCorrectToolForDrops()
            )
    );
}