package net.anarcho.pinsmodaddon.content.network.blockentities;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.utility.Lang;
import net.anarcho.pinsmodaddon.PneumaticItemNetworks;
import net.anarcho.pinsmodaddon.content.network.PneumaticNetworkManager;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PneumaticInjectorBlockEntity extends KineticBlockEntity {
    private final SmartInventory inventory;
    private final LazyOptional<IItemHandler> itemHandler;
    private int transferCooldown;
    private int itemsInjected;
    private boolean isInNetwork;

    private boolean lastLoggedStatus = false;
    private static final int LOG_COOLDOWN = 100; // ticks
    private int ticksSinceLastLog = 0;

    private static final int COOLDOWN_TIME = 10; // 10 ticks cooldown

    public PneumaticInjectorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        this.inventory = new SmartInventory(9, this);
        this.itemHandler = LazyOptional.of(() -> this.inventory);
        this.transferCooldown = 0;
        this.itemsInjected = 0;
        this.isInNetwork = false;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) {
            return;
        }

        if (transferCooldown > 0) {
            transferCooldown--;
            return;
        }

        boolean speedRequirement = isSpeedRequirementFulfilled();
        boolean hasItems = hasItemsToInject();

        if (speedRequirement != lastLoggedStatus || hasItems != lastLoggedStatus || ticksSinceLastLog >= LOG_COOLDOWN) {
            PneumaticItemNetworks.LOGGER.debug("PneumaticInjector at {} - Speed Requirement: {}, Has Items: {}", worldPosition, speedRequirement, hasItems);
            lastLoggedStatus = speedRequirement && hasItems;
            ticksSinceLastLog = 0;
        } else {
            ticksSinceLastLog++;
        }

        if (speedRequirement && hasItems) {
            if (!isInNetwork) {
                PneumaticNetworkManager.get(level).addInjector(worldPosition, inventory);
                isInNetwork = true;
                PneumaticItemNetworks.LOGGER.info("Added injector to network at {}", worldPosition);
            }
            transferItems();
        } else if (isInNetwork) {
            PneumaticNetworkManager.get(level).removeInjector(worldPosition);
            isInNetwork = false;
            PneumaticItemNetworks.LOGGER.info("Removed injector from network at {}", worldPosition);
        }
    }

    private boolean hasItemsToInject() {
        List<IItemHandler> adjacentInvs = getAdjacentInventories();
        for (IItemHandler handler : adjacentInvs) {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (!handler.getStackInSlot(i).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private void transferItems() {
        List<IItemHandler> adjacentInvs = getAdjacentInventories();
        for (IItemHandler sourceInv : adjacentInvs) {
            for (int sourceSlot = 0; sourceSlot < sourceInv.getSlots(); sourceSlot++) {
                ItemStack extractedStack = sourceInv.extractItem(sourceSlot, 64, true);
                if (!extractedStack.isEmpty()) {
                    for (int destSlot = 0; destSlot < inventory.getSlots(); destSlot++) {
                        ItemStack remaining = inventory.insertItem(destSlot, extractedStack, false);
                        int inserted = extractedStack.getCount() - remaining.getCount();
                        if (inserted > 0) {
                            sourceInv.extractItem(sourceSlot, inserted, false);
                            itemsInjected += inserted;
                            transferCooldown = COOLDOWN_TIME;
                            PneumaticItemNetworks.LOGGER.info("Injected {} items at {}", inserted, worldPosition);
                            return;
                        }
                    }
                }
            }
        }
    }

    private List<IItemHandler> getAdjacentInventories() {
        List<IItemHandler> inventories = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = worldPosition.relative(direction);
            BlockEntity adjacentBE = level.getBlockEntity(adjacentPos);
            if (adjacentBE != null) {
                adjacentBE.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite())
                        .ifPresent(inventories::add);
            }
        }
        return inventories;
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
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        inventory.deserializeNBT(compound.getCompound("Inventory"));
        itemsInjected = compound.getInt("ItemsInjected");
        transferCooldown = compound.getInt("TransferCooldown");
        isInNetwork = compound.getBoolean("IsInNetwork");
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.put("Inventory", inventory.serializeNBT());
        compound.putInt("ItemsInjected", itemsInjected);
        compound.putInt("TransferCooldown", transferCooldown);
        compound.putBoolean("IsInNetwork", isInNetwork);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        Lang.translate("tooltip.pneumatic_injector.header")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.translate("tooltip.pneumatic_injector.items_injected", itemsInjected)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        tooltip.add(Component.literal("Speed: " + getSpeed() + " RPM"));
        return true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && isInNetwork) {
            PneumaticNetworkManager.get(level).addInjector(worldPosition, inventory);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null && !level.isClientSide && isInNetwork) {
            PneumaticNetworkManager.get(level).removeInjector(worldPosition);
        }
    }
}