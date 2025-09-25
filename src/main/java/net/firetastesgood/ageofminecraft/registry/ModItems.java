package net.firetastesgood.ageofminecraft.registry;

import net.firetastesgood.ageofminecraft.EngenderMod;
import net.firetastesgood.ageofminecraft.items.*;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EngenderMod.MODID);

    // Mana Crystals I-X
    public static final RegistryObject<Item> MANA_CRYSTAL_1  = ITEMS.register("mana_crystal_1",  () -> new ManaCrystalItem(1, propsEpic()));
    public static final RegistryObject<Item> MANA_CRYSTAL_2  = ITEMS.register("mana_crystal_2",  () -> new ManaCrystalItem(2, propsEpic()));
    public static final RegistryObject<Item> MANA_CRYSTAL_3  = ITEMS.register("mana_crystal_3",  () -> new ManaCrystalItem(3, propsEpic()));
    public static final RegistryObject<Item> MANA_CRYSTAL_4  = ITEMS.register("mana_crystal_4",  () -> new ManaCrystalItem(4, propsEpic()));
    public static final RegistryObject<Item> MANA_CRYSTAL_5  = ITEMS.register("mana_crystal_5",  () -> new ManaCrystalItem(5, propsEpic()));
    public static final RegistryObject<Item> MANA_CRYSTAL_6  = ITEMS.register("mana_crystal_6",  () -> new ManaCrystalItem(6, propsEpic()));
    public static final RegistryObject<Item> MANA_CRYSTAL_7  = ITEMS.register("mana_crystal_7",  () -> new ManaCrystalItem(7, propsEpic()));
    public static final RegistryObject<Item> MANA_CRYSTAL_8  = ITEMS.register("mana_crystal_8",  () -> new ManaCrystalItem(8, propsEpic()));
    public static final RegistryObject<Item> MANA_CRYSTAL_9  = ITEMS.register("mana_crystal_9",  () -> new ManaCrystalItem(9, propsEpic()));
    public static final RegistryObject<Item> MANA_CRYSTAL_10 = ITEMS.register("mana_crystal_10", () -> new ManaCrystalItem(10, propsEpic()));

    // Entropy Crystals I-X
    public static final RegistryObject<Item> ENTROPY_CRYSTAL_1  = ITEMS.register("entropy_crystal_1",  () -> new EntropyCrystalItem(1, propsEpic()));
    public static final RegistryObject<Item> ENTROPY_CRYSTAL_2  = ITEMS.register("entropy_crystal_2",  () -> new EntropyCrystalItem(2, propsEpic()));
    public static final RegistryObject<Item> ENTROPY_CRYSTAL_3  = ITEMS.register("entropy_crystal_3",  () -> new EntropyCrystalItem(3, propsEpic()));
    public static final RegistryObject<Item> ENTROPY_CRYSTAL_4  = ITEMS.register("entropy_crystal_4",  () -> new EntropyCrystalItem(4, propsEpic()));
    public static final RegistryObject<Item> ENTROPY_CRYSTAL_5  = ITEMS.register("entropy_crystal_5",  () -> new EntropyCrystalItem(5, propsEpic()));
    public static final RegistryObject<Item> ENTROPY_CRYSTAL_6  = ITEMS.register("entropy_crystal_6",  () -> new EntropyCrystalItem(6, propsEpic()));
    public static final RegistryObject<Item> ENTROPY_CRYSTAL_7  = ITEMS.register("entropy_crystal_7",  () -> new EntropyCrystalItem(7, propsEpic()));
    public static final RegistryObject<Item> ENTROPY_CRYSTAL_8  = ITEMS.register("entropy_crystal_8",  () -> new EntropyCrystalItem(8, propsEpic()));
    public static final RegistryObject<Item> ENTROPY_CRYSTAL_9  = ITEMS.register("entropy_crystal_9",  () -> new EntropyCrystalItem(9, propsEpic()));
    public static final RegistryObject<Item> ENTROPY_CRYSTAL_10 = ITEMS.register("entropy_crystal_10", () -> new EntropyCrystalItem(10, propsEpic()));

    // Infinite Wellspring
    public static final RegistryObject<Item> INFINITE_WELLSPRING =
            ITEMS.register("infinite_wellspring", () -> new InfiniteWellspringItem(propsEpic()));

    public static final RegistryObject<Item> WOODEN_CLEAVER   = ITEMS.register("wooden_cleaver",
            () -> new CleaverItem(Tiers.WOOD,      3, -2.4f, propsWeapon()));
    public static final RegistryObject<Item> STONE_CLEAVER    = ITEMS.register("stone_cleaver",
            () -> new CleaverItem(Tiers.STONE,     3, -2.4f, propsWeapon()));
    public static final RegistryObject<Item> IRON_CLEAVER     = ITEMS.register("iron_cleaver",
            () -> new CleaverItem(Tiers.IRON,      3, -2.4f, propsWeapon()));
    public static final RegistryObject<Item> GOLDEN_CLEAVER   = ITEMS.register("golden_cleaver",
            () -> new CleaverItem(Tiers.GOLD,      3, -2.4f, propsWeapon()));
    public static final RegistryObject<Item> DIAMOND_CLEAVER  = ITEMS.register("diamond_cleaver",
            () -> new CleaverItem(Tiers.DIAMOND,   3, -2.4f, propsWeapon()));
    public static final RegistryObject<Item> NETHERITE_CLEAVER= ITEMS.register("netherite_cleaver",
            () -> new CleaverItem(Tiers.NETHERITE, 3, -2.4f, propsWeapon()));

    private static Item.Properties propsEpic() {
        return new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC);
    }

    private static Item.Properties propsWeapon() {
        return new Item.Properties().stacksTo(1);
    }

    public static final RegistryObject<net.minecraft.world.item.Item> FUSION_CRAFTER =
            ITEMS.register("fusion_crafter",
                    () -> new net.minecraft.world.item.BlockItem(ModBlocks.FUSION_CRAFTER.get(),
                            new net.minecraft.world.item.Item.Properties()));
}