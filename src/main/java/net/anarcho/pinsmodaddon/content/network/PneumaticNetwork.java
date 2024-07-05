package net.anarcho.pinsmodaddon.content.network;

import net.anarcho.pinsmodaddon.PneumaticItemNetworks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

public class PneumaticNetwork {
    private final Set<BlockPos> pipes = new HashSet<>();
    private final Map<BlockPos, IItemHandler> injectors = new HashMap<>();
    private final Map<BlockPos, IItemHandler> ejectors = new HashMap<>();

    private boolean lastLoggedActiveState = false;
    private static final int LOG_COOLDOWN = 100; // ticks
    private int ticksSinceLastLog = 0;


    private static final int MAX_TRANSFER_PER_TICK = 64;

    public void addPipe(BlockPos pos) {
        pipes.add(pos);
        PneumaticItemNetworks.LOGGER.debug("Added pipe at {}", pos);
    }

    public void removePipe(BlockPos pos) {
        pipes.remove(pos);
        PneumaticItemNetworks.LOGGER.debug("Removed pipe at {}", pos);
    }

    public void addInjector(BlockPos pos, IItemHandler itemHandler) {
        injectors.put(pos, itemHandler);
        PneumaticItemNetworks.LOGGER.debug("Added injector at {}", pos);
    }

    public void removeInjector(BlockPos pos) {
        injectors.remove(pos);
        PneumaticItemNetworks.LOGGER.debug("Removed injector at {}", pos);
    }

    public void addEjector(BlockPos pos, IItemHandler itemHandler) {
        ejectors.put(pos, itemHandler);
        PneumaticItemNetworks.LOGGER.debug("Added ejector at {}", pos);
    }

    public void removeEjector(BlockPos pos) {
        ejectors.remove(pos);
        PneumaticItemNetworks.LOGGER.debug("Removed ejector at {}", pos);
    }

    public void updateConnections(Level level, BlockPos pos) {
        pipes.add(pos);
        PneumaticItemNetworks.LOGGER.debug("Updated connections for pipe at {}", pos);
    }

    public void merge(PneumaticNetwork other) {
        pipes.addAll(other.pipes);
        injectors.putAll(other.injectors);
        ejectors.putAll(other.ejectors);
        PneumaticItemNetworks.LOGGER.debug("Merged networks. New size: {} pipes, {} injectors, {} ejectors",
                pipes.size(), injectors.size(), ejectors.size());
    }

    public Set<BlockPos> getAllPositions() {
        Set<BlockPos> allPositions = new HashSet<>(pipes);
        allPositions.addAll(injectors.keySet());
        allPositions.addAll(ejectors.keySet());
        return allPositions;
    }

    public boolean isEmpty() {
        return pipes.isEmpty() && injectors.isEmpty() && ejectors.isEmpty();
    }

    public boolean isActive() {
        boolean active = !injectors.isEmpty() && !ejectors.isEmpty();
        PneumaticItemNetworks.LOGGER.debug("Network active status: {}. Injectors: {}, Ejectors: {}, Pipes: {}",
                active, injectors.size(), ejectors.size(), pipes.size());
        return active;
    }

    public void tick() {
        boolean isActive = isActive();
        if (isActive != lastLoggedActiveState || ticksSinceLastLog >= LOG_COOLDOWN) {
            PneumaticItemNetworks.LOGGER.debug("Network active status: {}. Injectors: {}, Ejectors: {}, Pipes: {}",
                    isActive, injectors.size(), ejectors.size(), pipes.size());
            lastLoggedActiveState = isActive;
            ticksSinceLastLog = 0;
        } else {
            ticksSinceLastLog++;
        }

        if (!isActive) {
            return;
        }

        PneumaticItemNetworks.LOGGER.debug("Ticking network with {} injectors and {} ejectors", injectors.size(), ejectors.size());

        for (Map.Entry<BlockPos, IItemHandler> entry : injectors.entrySet()) {
            IItemHandler injector = entry.getValue();
            BlockPos injectorPos = entry.getKey();
            transferItems(injector, injectorPos);
        }
    }

    private void transferItems(IItemHandler injector, BlockPos injectorPos) {
        for (int slot = 0; slot < injector.getSlots(); slot++) {
            ItemStack extracted = injector.extractItem(slot, MAX_TRANSFER_PER_TICK, true);
            if (!extracted.isEmpty()) {
                PneumaticItemNetworks.LOGGER.debug("Attempting to extract {} items from injector at {}", extracted.getCount(), injectorPos);

                for (Map.Entry<BlockPos, IItemHandler> ejectorEntry : ejectors.entrySet()) {
                    IItemHandler ejector = ejectorEntry.getValue();
                    BlockPos ejectorPos = ejectorEntry.getKey();

                    int inserted = insertIntoEjector(ejector, extracted);
                    if (inserted > 0) {
                        ItemStack actualExtracted = injector.extractItem(slot, inserted, false);
                        PneumaticItemNetworks.LOGGER.debug("Transferred {} items from injector at {} to ejector at {}", inserted, injectorPos, ejectorPos);

                        if (inserted == extracted.getCount()) {
                            break;
                        }
                        extracted = ItemStack.EMPTY;
                    }
                }
            }
        }
    }

    private int insertIntoEjector(IItemHandler ejector, ItemStack stack) {
        int totalInserted = 0;
        ItemStack remaining = stack.copy();

        for (int ejectorSlot = 0; ejectorSlot < ejector.getSlots() && !remaining.isEmpty(); ejectorSlot++) {
            ItemStack simulatedInsert = ejector.insertItem(ejectorSlot, remaining, true);
            int insertedThisSlot = remaining.getCount() - simulatedInsert.getCount();

            if (insertedThisSlot > 0) {
                ejector.insertItem(ejectorSlot, remaining.split(insertedThisSlot), false);
                totalInserted += insertedThisSlot;
            }
        }

        return totalInserted;
    }

    public int getPipeCount() {
        return pipes.size();
    }

    public int getInjectorCount() {
        return injectors.size();
    }

    public int getEjectorCount() {
        return ejectors.size();
    }
}