package net.firetastesgood.ageofminecraft.fusion;

import net.firetastesgood.ageofminecraft.registry.ModRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class FusionRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Item input;
    private final ItemStack output;
    private final int duration;
    private final int manaCost;
    private final int entropyCost;

    public FusionRecipe(ResourceLocation id, Item input, ItemStack output, int duration, int manaCost, int entropyCost) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.duration = duration;
        this.manaCost = manaCost;
        this.entropyCost = entropyCost;
    }

    public static FusionRecipe find(Level level, ItemStack part) {
        if (level == null || part.isEmpty()) return null;
        RecipeManager rm = level.getRecipeManager();
        RecipeType<FusionRecipe> type = ModRecipeTypes.FUSION_CRAFTING.get();
        for (FusionRecipe r : rm.getAllRecipesFor(type)) {
            if (part.is(r.input)) return r;
        }
        return null;
    }

    public int duration()     { return duration; }
    public int manaCost()     { return manaCost; }
    public int entropyCost()  { return entropyCost; }
    public ItemStack output() { return output; }

    @Override public boolean matches(Container inv, Level level) {
        ItemStack in = inv.getItem(0);
        return !in.isEmpty() && in.is(input);
    }

    @Override public ItemStack assemble(Container inv, RegistryAccess access) {
        return output.copy();
    }

    @Override public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override public ItemStack getResultItem(RegistryAccess access) {
        return output.copy();
    }

    @Override public ResourceLocation getId() {
        return id;
    }

    @Override public RecipeSerializer<?> getSerializer() {
        return ModRecipeTypes.FUSION_CRAFTING_SERIALIZER.get();
    }

    @Override public RecipeType<?> getType() {
        return ModRecipeTypes.FUSION_CRAFTING.get();
    }

    @Override public boolean isSpecial() {
        return true;
    }

    public Item inputItem() { return input; }
}