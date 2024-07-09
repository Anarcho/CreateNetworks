package net.anarcho.pinsmodaddon.content.menus;

import com.simibubi.create.foundation.gui.menu.MenuBase;
import net.anarcho.pinsmodaddon.content.blockentities.PunchcardProgrammerBlockEntity;
import net.anarcho.pinsmodaddon.content.items.PunchcardItem;
import net.anarcho.pinsmodaddon.registry.ModBlockEntities;
import net.anarcho.pinsmodaddon.registry.ModBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class PunchcardProgrammerMenu extends MenuBase<PunchcardProgrammerBlockEntity> {

    private static final int GRID_SIZE = 5;
    private final String[][] programGrid = new String[GRID_SIZE][GRID_SIZE];
    private int energyStored;

    public PunchcardProgrammerMenu(MenuType<?> type, int windowId, Inventory inv, PunchcardProgrammerBlockEntity be) {
        super(type, windowId, inv, be);
    }

    public PunchcardProgrammerMenu(MenuType<?> type, int windowId, Inventory inv, FriendlyByteBuf data) {
        this(type, windowId, inv, getBlockEntity(inv, data));
    }

    private static PunchcardProgrammerBlockEntity getBlockEntity(Inventory inv, FriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        return (PunchcardProgrammerBlockEntity) inv.player.level().getBlockEntity(pos);
    }

    @Override
    protected PunchcardProgrammerBlockEntity createOnClient(FriendlyByteBuf extraData) {
        return new PunchcardProgrammerBlockEntity(
                ModBlockEntities.PUNCHCARD_PROGRAMMER.get(),
                extraData.readBlockPos(),
                ModBlocks.PUNCHCARD_PROGRAMMER.getDefaultState()
        );
    }

    @Override
    protected void initAndReadInventory(PunchcardProgrammerBlockEntity be) {
        energyStored = be.getCapability(ForgeCapabilities.ENERGY)
                .map(IEnergyStorage::getEnergyStored)
                .orElse(0);
    }

    @Override
    protected void addSlots() {
        addPunchcardSlots();
        addPlayerSlots(8, 84);
    }

    private void addPunchcardSlots() {
        this.addSlot(new SlotItemHandler(contentHolder.getInventory(), 0, 27, 47) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof PunchcardItem;
            }
        });  // Input slot
        this.addSlot(new SlotItemHandler(contentHolder.getInventory(), 1, 134, 47) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;  // Output slot should not accept manual insertion
            }
        });  // Output slot
    }

    @Override
    protected void saveData(PunchcardProgrammerBlockEntity be) {
        // Save data if needed
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 2) {
                if (!this.moveItemStackTo(itemstack1, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public void setProgramGridCell(int row, int col, String value) {
        if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
            programGrid[row][col] = value;
        }
    }

    public String getProgramGridCell(int row, int col) {
        if (row >= 0 && row < GRID_SIZE && col >= 0 && col < GRID_SIZE) {
            return programGrid[row][col];
        }
        return "";
    }

    public void programPunchcard() {
        if (contentHolder.canProgramPunchcard()) {
            contentHolder.programPunchcard(programGrid);
            // Clear the program grid after programming
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    programGrid[i][j] = "";
                }
            }
        }
    }

    public void chainPunchcards() {
        contentHolder.chainPunchcards();
    }

    public void copyPunchcard() {
        contentHolder.copyPunchcard();
    }

    public int getEnergyStored() {
        return energyStored;
    }

    public void setEnergyStored(int energy) {
        this.energyStored = energy;
    }
}
