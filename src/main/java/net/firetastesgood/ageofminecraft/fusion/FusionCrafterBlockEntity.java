package net.firetastesgood.ageofminecraft.fusion;

import net.firetastesgood.ageofminecraft.entity.EntropyOrbEntity;
import net.firetastesgood.ageofminecraft.entity.ManaOrbEntity;
import net.firetastesgood.ageofminecraft.items.EntropyCrystalItem;
import net.firetastesgood.ageofminecraft.items.ManaCrystalItem;
import net.firetastesgood.ageofminecraft.registry.ModBlockEntities;
import net.firetastesgood.ageofminecraft.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import static net.firetastesgood.ageofminecraft.registry.ModTags.Items.CRYSTALS;
import static net.firetastesgood.ageofminecraft.registry.ModTags.Items.FUSION_PARTS;

public class FusionCrafterBlockEntity extends BlockEntity implements MenuProvider, Clearable {

    private final SimpleContainer items = new SimpleContainer(3) {
        @Override public int getMaxStackSize() { return 64; }
        @Override public boolean canPlaceItem(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.is(FUSION_PARTS);
                case 1 -> stack.is(CRYSTALS);
                case 2 -> false;
                default -> false;
            };
        }
    };

    public static final int MAX_MANA = 2_000_000;
    public static final int MAX_ENTROPY = 20_000;

    private static final int RATE_MANA = 200;
    private static final int RATE_ENTROPY = 50;

    private int mana;
    private int entropy;

    private int progress;
    private int total;

    private int manaFrac;
    private int entropyFrac;
    private int perTickDen;

    private FusionRecipe lockedRecipe;

    private final ContainerData dataAccess = new SimpleContainerData(4) {
        @Override public int get(int i) {
            return switch (i) {
                case 0 -> progress;
                case 1 -> Math.max(1, total);
                case 2 -> mana;
                case 3 -> entropy;
                default -> 0;
            };
        }

        @Override public void set(int i, int v) {
            switch (i) {
                case 0 -> progress = v;
                case 1 -> total = v;
                case 2 -> mana = v;
                case 3 -> entropy = v;
            }
        }
        @Override public int getCount() { return 4; }
    };

    public FusionCrafterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_CRAFTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FusionCrafterBlockEntity be) {
        be.drainCrystalSlot();
        be.craftTick();
    }

    private void drainCrystalSlot() {
        ItemStack s = items.getItem(1);
        if (s.isEmpty()) return;

        if (s.getItem() == ModItems.INFINITE_WELLSPRING.get()) {
            CompoundTag tag = s.getOrCreateTag();
            int storedMana    = tag.getInt("Mana");
            int storedEntropy = tag.getInt("Entropy");

            int added = 0;

            if (this.mana < MAX_MANA && storedMana > 0) {
                int giveM = Math.min(RATE_MANA, Math.min(MAX_MANA - this.mana, storedMana));
                if (giveM > 0) {
                    this.mana += giveM;
                    tag.putInt("Mana", storedMana - giveM);
                    added |= giveM;
                }
            }

            if (this.entropy < MAX_ENTROPY && storedEntropy > 0) {
                int giveE = Math.min(RATE_ENTROPY, Math.min(MAX_ENTROPY - this.entropy, storedEntropy));
                if (giveE > 0) {
                    this.entropy += giveE;
                    tag.putInt("Entropy", storedEntropy - giveE);
                    added |= giveE;
                }
            }

            if (added != 0) this.setChanged();

            if (storedMana > 0 && tag.getInt("Mana") == 0) {
                level.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 0.5f);
            }
            if (storedEntropy > 0 && tag.getInt("Entropy") == 0) {
                level.playSound(null, worldPosition, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 0.1f, 1.0f);
            }
        }

        if (s.getItem() instanceof ManaCrystalItem mc && mana < MAX_MANA) {
            int cur = mc.getCurrent(s);
            if (cur > 0) {
                int room = MAX_MANA - mana;
                int take = Math.min(cur, Math.min(RATE_MANA, room));
                if (take > 0) {
                    mc.addMana(s, -take);
                    mana += take;
                    setChanged();
                }
                if (mc.getCurrent(s) == 0) {
                    level.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 0.5f);
                }
            }
        }

        if (s.getItem() instanceof EntropyCrystalItem ec && entropy < MAX_ENTROPY) {
            int cur = ec.getCurrent(s);
            if (cur > 0) {
                int room = MAX_ENTROPY - entropy;
                int take = Math.min(cur, Math.min(RATE_ENTROPY, room));
                if (take > 0) {
                    ec.addEntropy(s, -take);
                    entropy += take;
                    setChanged();
                }
                if (ec.getCurrent(s) == 0) {
                    level.playSound(null, worldPosition, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 0.1f, 1.0f);
                }
            }
        }
    }

    private void craftTick() {
        ItemStack part = items.getItem(0);
        if (part.isEmpty()) { resetIfActive(); return; }
        if (!hasOutputRoom()) return;

        if (lockedRecipe != null) {
            FusionRecipe check = FusionRecipe.find(level, part);
            if (check == null || check != lockedRecipe) {
                resetIfActive();
                return;
            }
        }

        FusionRecipe recipe = (lockedRecipe != null) ? lockedRecipe : FusionRecipe.find(level, part);
        if (recipe == null) { resetIfActive(); return; }

        if (progress == 0) {
            lockedRecipe = recipe;
            total = Math.max(1, recipe.duration());
            perTickDen = total;
            manaFrac = 0;
            entropyFrac = 0;
        }

        boolean advanced = consumePerTick(recipe.manaCost(), recipe.entropyCost());
        if (!advanced) return;

        progress++;
        if (progress >= total) finishCraft(recipe);
    }

    private boolean consumePerTick(int manaNum, int entropyNum) {
        manaFrac += manaNum;
        entropyFrac += entropyNum;

        int needMana = 0, needEntropy = 0;
        while (manaFrac >= perTickDen) { needMana++; manaFrac -= perTickDen; }
        while (entropyFrac >= perTickDen) { needEntropy++; entropyFrac -= perTickDen; }

        if (mana < needMana || entropy < needEntropy) {
            manaFrac += needMana * perTickDen;
            entropyFrac += needEntropy * perTickDen;
            return false;
        }

        mana -= needMana;
        entropy -= needEntropy;
        setChanged();
        return true;
    }

    private void finishCraft(FusionRecipe recipe) {
        items.getItem(0).shrink(1);

        ItemStack out = recipe.getResultItem(level.registryAccess()).copy();
        ItemStack cur = items.getItem(2);
        if (cur.isEmpty()) {
            items.setItem(2, out);
        } else if (ItemStack.isSameItemSameTags(cur, out)) {
            cur.grow(out.getCount());
        }

        level.playSound(null, worldPosition, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0f, 2.0f);

        progress = 0;
        total = 0;
        manaFrac = 0;
        entropyFrac = 0;
        perTickDen = 0;
        lockedRecipe = null;
        setChanged();
    }

    private boolean hasOutputRoom() {
        ItemStack cur = items.getItem(2);
        if (cur.isEmpty()) return true;
        FusionRecipe r = (lockedRecipe != null) ? lockedRecipe : FusionRecipe.find(level, items.getItem(0));
        if (r == null) return false;
        ItemStack out = r.getResultItem(level.registryAccess()).copy();
        if (!ItemStack.isSameItemSameTags(cur, out)) return false;
        int result = cur.getCount() + out.getCount();
        return result <= cur.getMaxStackSize();
    }

    private void resetIfActive() {
        if (progress > 0 || lockedRecipe != null) {
            progress = 0;
            manaFrac = 0;
            entropyFrac = 0;
            perTickDen = 0;
            lockedRecipe = null;
            setChanged();
        }
    }

    public void dropContentsAndRefundEnergy() {
        for (int i = 0; i < items.getContainerSize(); i++) {
            ItemStack s = items.getItem(i);
            if (!s.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level,
                        worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                        s.copy()));
            }
        }
        items.clearContent();

        if (mana > 0) {
            level.addFreshEntity(new ManaOrbEntity(level,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, mana));
        }
        if (entropy > 0) {
            level.addFreshEntity(new EntropyOrbEntity(level,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, entropy));
        }
        mana = 0;
        entropy = 0;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("mana", mana);
        tag.putInt("entropy", entropy);
        tag.putInt("progress", progress);
        tag.putInt("total", total);
        tag.putInt("manaFrac", manaFrac);
        tag.putInt("entropyFrac", entropyFrac);
        tag.putInt("perTickDen", perTickDen);
        for (int i = 0; i < items.getContainerSize(); i++) {
            tag.put("it" + i, items.getItem(i).save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mana = tag.getInt("mana");
        entropy = tag.getInt("entropy");
        progress = tag.getInt("progress");
        total = tag.getInt("total");
        manaFrac = tag.getInt("manaFrac");
        entropyFrac = tag.getInt("entropyFrac");
        perTickDen = tag.getInt("perTickDen");
        for (int i = 0; i < items.getContainerSize(); i++) {
            items.setItem(i, ItemStack.of(tag.getCompound("it" + i)));
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ageofminecraft.fusion_crafter");
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
        return new FusionCrafterMenu(windowId, playerInv, this.level, this.worldPosition, this.dataAccess);
    }

    public SimpleContainer getItems() { return items; }
    public ContainerData getData() { return dataAccess; }

    @Override
    public void clearContent() {
        items.clearContent();
        setChanged();
    }
}