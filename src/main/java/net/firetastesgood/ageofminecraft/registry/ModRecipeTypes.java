package net.firetastesgood.ageofminecraft.registry;

import net.firetastesgood.ageofminecraft.fusion.FusionRecipe;
import net.firetastesgood.ageofminecraft.fusion.FusionRecipeSerializer;
import net.firetastesgood.ageofminecraft.fusion.FusionRecipeType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, "ageofminecraft");
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "ageofminecraft");

    public static final RegistryObject<RecipeType<FusionRecipe>> FUSION_CRAFTING =
            RECIPE_TYPES.register("fusion_crafting", FusionRecipeType::new);

    public static final RegistryObject<RecipeSerializer<FusionRecipe>> FUSION_CRAFTING_SERIALIZER =
            RECIPE_SERIALIZERS.register("fusion_crafting", FusionRecipeSerializer::new);
}