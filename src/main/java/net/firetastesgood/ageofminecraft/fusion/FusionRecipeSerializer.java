package net.firetastesgood.ageofminecraft.fusion;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FusionRecipeSerializer implements RecipeSerializer<FusionRecipe> {

    @Override
    public FusionRecipe fromJson(ResourceLocation id, JsonObject json) {
        ResourceLocation inId = new ResourceLocation(
                json.getAsJsonObject("catalyst").get("item").getAsString());

        JsonObject outObj = json.getAsJsonObject("result");
        ResourceLocation outId = new ResourceLocation(outObj.get("item").getAsString());
        int count = outObj.has("count") ? outObj.get("count").getAsInt() : 1;

        int duration = json.get("duration").getAsInt();
        int mana     = json.get("mana_cost").getAsInt();
        int entropy  = json.get("entropy_cost").getAsInt();

        Item inputItem = BuiltInRegistries.ITEM.get(inId);
        Item outItem   = BuiltInRegistries.ITEM.get(outId);

        return new FusionRecipe(id, inputItem, new ItemStack(outItem, count), duration, mana, entropy);
    }

    @Override
    public FusionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        ResourceLocation inRL = buf.readResourceLocation();
        Item inputItem = BuiltInRegistries.ITEM.get(inRL);

        ItemStack out = buf.readItem();

        int duration = buf.readVarInt();
        int mana     = buf.readVarInt();
        int entropy  = buf.readVarInt();

        return new FusionRecipe(id, inputItem, out, duration, mana, entropy);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, FusionRecipe r) {
        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(r.inputItem()));

        buf.writeItem(r.output());

        buf.writeVarInt(r.duration());
        buf.writeVarInt(r.manaCost());
        buf.writeVarInt(r.entropyCost());
    }
}