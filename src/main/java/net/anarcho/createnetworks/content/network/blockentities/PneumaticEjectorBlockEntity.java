package net.anarcho.createnetworks.content.network.blockentities;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.utility.Lang;
import net.anarcho.createnetworks.CreateNetworks;
import net.anarcho.createnetworks.content.network.PneumaticNetworkManager;
import net.anarcho.createnetworks.content.network.blocks.PneumaticEjectorBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PneumaticEjectorBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {

    private final SmartInventory inventory;
    private final LazyOptional<IItemHandler> itemHandler;
    private int transferCooldown;
    private int itemsExtracted;
    private boolean isInNetwork;

    private boolean lastLoggedStatus = false;
    private static final int LOG_COOLDOWN = 100; // ticks
    private int ticksSinceLastLog = 0;

    private static final int COOLDOWN_TIME = 10; // 10 ticks cooldown

    public PneumaticEjectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inventory = new SmartInventory(9, this);
        this.itemHandler = LazyOptional.of(() -> this.inventory);
        this.transferCooldown = 0;
        this.itemsExtracted = 0;
        this.isInNetwork = false;
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide())
            return;

        if (transferCooldown > 0) {
            transferCooldown--;
            return;
        }

        boolean speedRequirement = isSpeedRequirementFulfilled();
        boolean isPowered = level.hasNeighborSignal(worldPosition) == getBlockState().getValue(PneumaticEjectorBlock.POWERED);

        if (speedRequirement != lastLoggedStatus || isPowered != lastLoggedStatus || ticksSinceLastLog >= LOG_COOLDOWN) {
            CreateNetworks.LOGGER.debug("PneumaticEjector at {} - Speed Requirement: {}, Is Powered: {}", worldPosition, speedRequirement, isPowered);
            lastLoggedStatus = speedRequirement && isPowered;
            ticksSinceLastLog = 0;
        } else {
            ticksSinceLastLog++;
        }

        if (speedRequirement && isPowered) {
            if (!isInNetwork) {
                PneumaticNetworkManager.get(level).addEjector(worldPosition, inventory);
                isInNetwork = true;
                CreateNetworks.LOGGER.info("Added ejector to network at {}", worldPosition);
            }
            ejectItems();
        } else if (isInNetwork) {
            PneumaticNetworkManager.get(level).removeEjector(worldPosition);
            isInNetwork = false;
            CreateNetworks.LOGGER.info("Removed ejector from network at {}", worldPosition);
        }
    }

    private boolean ejectItems() {
        List<IItemHandler> adjacentInvs = getAdjacentInventories();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack toEject = inventory.extractItem(i, 64, true);
            if (!toEject.isEmpty()) {
                for (IItemHandler adjacentInv : adjacentInvs) {
                    for (int j = 0; j < adjacentInv.getSlots(); j++) {
                        ItemStack remaining = adjacentInv.insertItem(j, toEject, false);
                        int ejected = toEject.getCount() - remaining.getCount();
                        if (ejected > 0) {
                            inventory.extractItem(i, ejected, false);
                            itemsExtracted += ejected;
                            transferCooldown = COOLDOWN_TIME;
                            CreateNetworks.LOGGER.info("Ejector at {} ejected {} items to adjacent inventory", worldPosition, ejected);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private List<IItemHandler> getAdjacentInventories() {
        List<IItemHandler> inventories = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            if (level != null) {
                BlockEntity adjacentBE = level.getBlockEntity(adjacentPos);
                if (adjacentBE != null) {
                    adjacentBE.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite())
                            .ifPresent(inventories::add);
                }
            }
        }
        return inventories;
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        inventory.deserializeNBT(compound.getCompound("Inventory"));
        itemsExtracted = compound.getInt("ItemsExtracted");
        transferCooldown = compound.getInt("TransferCooldown");
        isInNetwork = compound.getBoolean("IsInNetwork");
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.put("Inventory", inventory.serializeNBT());
        compound.putInt("ItemsExtracted", itemsExtracted);
        compound.putInt("TransferCooldown", transferCooldown);
        compound.putBoolean("IsInNetwork", isInNetwork);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("tooltip.pneumatic_ejector.header")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.translate("tooltip.pneumatic_ejector.items_extracted", itemsExtracted)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        return true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && isInNetwork) {
            PneumaticNetworkManager.get(level).addEjector(worldPosition, inventory);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null && !level.isClientSide && isInNetwork) {
            PneumaticNetworkManager.get(level).removeEjector(worldPosition);
        }
    }
}