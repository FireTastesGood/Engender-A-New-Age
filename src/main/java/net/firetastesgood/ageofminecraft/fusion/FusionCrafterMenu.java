package net.firetastesgood.ageofminecraft.fusion;

import net.firetastesgood.ageofminecraft.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import static net.firetastesgood.ageofminecraft.registry.ModTags.Items.CRYSTALS;
import static net.firetastesgood.ageofminecraft.registry.ModTags.Items.FUSION_PARTS;

public class FusionCrafterMenu extends AbstractContainerMenu {
    private final Level level;
    private final BlockPos pos;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public ContainerData data() { return data; }

    public FusionCrafterMenu(int id, Inventory inv, Level level, BlockPos pos, ContainerData data) {
        super(ModMenus.FUSION_CRAFTER.get(), id);
        this.level = level;
        this.pos = pos;
        this.data = data;
        this.access = ContainerLevelAccess.create(level, pos);

        var be = level.getBlockEntity(pos);
        if (be instanceof FusionCrafterBlockEntity fc) {
            this.addSlot(new Slot(fc.getItems(), 0, 79, 17) {
                @Override public boolean mayPlace(ItemStack stack) { return stack.is(FUSION_PARTS); }
            });

            this.addSlot(new Slot(fc.getItems(), 1, 34, 58) {
                @Override public boolean mayPlace(ItemStack stack) { return stack.is(CRYSTALS); }
            });

            this.addSlot(new Slot(fc.getItems(), 2, 79, 58) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
            });
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int hot = 0; hot < 9; ++hot) {
            this.addSlot(new Slot(inv, hot, 8 + hot * 18, 142));
        }

        this.addDataSlots(this.data);
    }

    public FusionCrafterMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level(), buf.readBlockPos(), new SimpleContainerData(4));
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate(
                (lvl, p) -> player.distanceToSqr(p.getX() + 0.5D, p.getY() + 0.5D, p.getZ() + 0.5D) <= 64.0D,
                true
        );
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ret;

        ItemStack in = slot.getItem();
        ret = in.copy();

        final int MACHINE_START = 0;
        final int MACHINE_END   = 3;
        final int INV_START     = MACHINE_END;
        final int INV_END       = INV_START + 27;
        final int HOT_START     = INV_END;
        final int HOT_END       = HOT_START + 9;
        final int PLAYER_START  = INV_START;
        final int PLAYER_END    = HOT_END;

        if (index < MACHINE_END) {
            if (!this.moveItemStackTo(in, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
        } else {
            if (in.is(FUSION_PARTS)) {
                if (!this.moveItemStackTo(in, 0, 1, false)) return ItemStack.EMPTY;
            } else if (in.is(CRYSTALS)) {
                if (!this.moveItemStackTo(in, 1, 2, false)) return ItemStack.EMPTY;
            } else {
                if (index < INV_END) {
                    if (!this.moveItemStackTo(in, HOT_START, HOT_END, false)) return ItemStack.EMPTY;
                } else if (!this.moveItemStackTo(in, INV_START, INV_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (in.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        slot.onTake(player, in);
        return ret;
    }
}