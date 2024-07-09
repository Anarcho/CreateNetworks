package net.anarcho.pinsmodaddon.content.blockentities;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.SmartInventory;
import net.anarcho.pinsmodaddon.content.items.PunchcardItem;
import net.anarcho.pinsmodaddon.content.menus.PunchcardProgrammerMenu;
import net.anarcho.pinsmodaddon.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PunchcardProgrammerBlockEntity extends SmartBlockEntity implements MenuProvider {

    private static final int ENERGY_CAPACITY = 10000;
    private static final int ENERGY_MAX_RECEIVE = 100;
    private static final int ENERGY_MAX_EXTRACT = 0;

    private final SmartInventory inventory;
    private LazyOptional<IItemHandler> itemHandler;
    private final EnergyStorage energyStorage;
    private LazyOptional<IEnergyStorage> energyHandler;

    public PunchcardProgrammerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inventory = new SmartInventory(2, this);
        this.itemHandler = LazyOptional.of(() -> this.inventory);
        this.energyStorage = new EnergyStorage(ENERGY_CAPACITY, ENERGY_MAX_RECEIVE, ENERGY_MAX_EXTRACT);
        this.energyHandler = LazyOptional.of(() -> this.energyStorage);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // Add behaviors if needed
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        inventory.deserializeNBT(compound.getCompound("Inventory"));
        energyStorage.deserializeNBT(compound.getCompound("Energy"));
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.put("Inventory", inventory.serializeNBT());
        compound.put("Energy", energyStorage.serializeNBT());
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Punchcard Programmer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
        return new PunchcardProgrammerMenu(ModMenuTypes.PUNCHCARD_PROGRAMMER.get(), id, playerInventory, this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        energyHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.itemHandler = LazyOptional.of(() -> this.inventory);
        this.energyHandler = LazyOptional.of(() -> this.energyStorage);
    }

    public SmartInventory getInventory() {
        return inventory;
    }

    public boolean hasEnoughEnergy() {
        return energyStorage.getEnergyStored() >= 100; // Assuming 100 energy per operation
    }

    public void consumeEnergy() {
        energyStorage.extractEnergy(100, false);
    }

    public boolean canProgramPunchcard() {
        ItemStack inputStack = inventory.getStackInSlot(0);
        ItemStack outputStack = inventory.getStackInSlot(1);
        return !inputStack.isEmpty() && inputStack.getItem() instanceof PunchcardItem &&
                outputStack.isEmpty() && hasEnoughEnergy();
    }

    public void programPunchcard(String[][] instructions) {
        if (canProgramPunchcard()) {
            ItemStack inputStack = inventory.getStackInSlot(0);
            ItemStack programmedCard = PunchcardItem.copyPunchcard(inputStack);

            for (int row = 0; row < instructions.length; row++) {
                for (int col = 0; col < instructions[row].length; col++) {
                    PunchcardItem.setInstruction(programmedCard, row, col, instructions[row][col]);
                }
            }

            inventory.extractItem(0, 1, false);
            inventory.insertItem(1, programmedCard, false);
            consumeEnergy();
            setChanged();
        }
    }

    public void chainPunchcards() {
        ItemStack card1 = inventory.getStackInSlot(0);
        ItemStack card2 = inventory.getStackInSlot(1);
        if (!card1.isEmpty() && !card2.isEmpty() &&
                card1.getItem() instanceof PunchcardItem && card2.getItem() instanceof PunchcardItem) {
            PunchcardItem.chainCard(card1, card2);
            setChanged();
        }
    }

    public void copyPunchcard() {
        ItemStack sourceCard = inventory.getStackInSlot(0);
        if (!sourceCard.isEmpty() && sourceCard.getItem() instanceof PunchcardItem &&
                inventory.getStackInSlot(1).isEmpty() && hasEnoughEnergy()) {
            ItemStack copiedCard = PunchcardItem.copyPunchcard(sourceCard);
            inventory.insertItem(1, copiedCard, false);
            consumeEnergy();
            setChanged();
        }
    }
}