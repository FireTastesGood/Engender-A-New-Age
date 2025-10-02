package net.firetastesgood.ageofminecraft.fusion;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FusionRecipeSerializer implements RecipeSerializer<FusionRecipe> {

    @Override
    public FusionRecipe fromJson(ResourceLocation id, JsonObject json) {
        JsonObject cat = GsonHelper.getAsJsonObject(json, "catalyst");
        ResourceLocation inId = new ResourceLocation(GsonHelper.getAsString(cat, "item"));
        Item inputItem = BuiltInRegistries.ITEM.get(inId);

        JsonObject outObj = GsonHelper.getAsJsonObject(json, "result");
        ResourceLocation outId = new ResourceLocation(GsonHelper.getAsString(outObj, "item"));
        int count = GsonHelper.getAsInt(outObj, "count", 1);
        ItemStack out = new ItemStack(BuiltInRegistries.ITEM.get(outId), count);

        int duration = GsonHelper.getAsInt(json, "duration", 200);
        int mana     = GsonHelper.getAsInt(json, "mana_cost", 0);
        int entropy  = GsonHelper.getAsInt(json, "entropy_cost", 0);

        ResourceLocation mobId = new ResourceLocation(GsonHelper.getAsString(json, "mob_id"));
        int firstXp            = GsonHelper.getAsInt(json, "first_spawn_xp", 0);
        double atkOverride     = GsonHelper.getAsDouble(json, "attack_damage_override", -1.0);

        return new FusionRecipe(id, inputItem, out, duration, mana, entropy, mobId, firstXp, atkOverride);
    }

    @Override
    public FusionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        ResourceLocation inRL = buf.readResourceLocation();
        Item inputItem = BuiltInRegistries.ITEM.get(inRL);

        ItemStack out = buf.readItem();

        int duration = buf.readVarInt();
        int mana     = buf.readVarInt();
        int entropy  = buf.readVarInt();

        ResourceLocation mobId = buf.readResourceLocation();
        int firstXp            = buf.readVarInt();
        double atkOverride     = buf.readDouble();

        return new FusionRecipe(id, inputItem, out, duration, mana, entropy, mobId, firstXp, atkOverride);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, FusionRecipe r) {
        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(r.inputItem()));
        buf.writeItem(r.output());

        buf.writeVarInt(r.duration());
        buf.writeVarInt(r.manaCost());
        buf.writeVarInt(r.entropyCost());

        buf.writeResourceLocation(r.mobId());
        buf.writeVarInt(r.firstSpawnXp());
        buf.writeDouble(r.attackDamageOverride());
    }
}