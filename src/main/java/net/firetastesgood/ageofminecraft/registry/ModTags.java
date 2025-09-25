package net.firetastesgood.ageofminecraft.registry;

import net.firetastesgood.ageofminecraft.EngenderMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

public final class ModTags {

    public static final class EntityTypes {
        public static final TagKey<EntityType<?>> END_RESIDENTS = TagKey.create(
                Registries.ENTITY_TYPE, new ResourceLocation(EngenderMod.MODID, "end_residents"));
        public static final TagKey<EntityType<?>> ILLAGER_RAID_MOBS = TagKey.create(
                Registries.ENTITY_TYPE, new ResourceLocation(EngenderMod.MODID, "illager_raid_mobs"));
        public static final TagKey<EntityType<?>> GOLEMS = TagKey.create(
                Registries.ENTITY_TYPE, new ResourceLocation(EngenderMod.MODID, "golems"));

        private EntityTypes() {}
    }

    public static final class Items {
        public static final TagKey<Item> CRYSTALS = TagKey.create(
                Registries.ITEM, new ResourceLocation(EngenderMod.MODID, "crystals"));
        public static final TagKey<Item> FUSION_PARTS = TagKey.create(
                Registries.ITEM, new ResourceLocation(EngenderMod.MODID, "fusion_parts"));

        private Items() {}
    }

    private ModTags() {}
}