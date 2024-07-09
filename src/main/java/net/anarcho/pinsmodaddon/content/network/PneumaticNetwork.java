package net.anarcho.pinsmodaddon.content.network;

import net.anarcho.pinsmodaddon.PneumaticItemNetworks;
import net.anarcho.pinsmodaddon.content.items.PunchcardData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

public class PneumaticNetwork {
    private final Set<BlockPos> pipes = new HashSet<>();
    private final Map<BlockPos, IItemHandler> injectors = new HashMap<>();
    private final Map<BlockPos, IItemHandler> ejectors = new HashMap<>();
    private final Map<BlockPos, PunchcardData> punchcardInstructions = new HashMap<>();

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
        punchcardInstructions.putAll(other.punchcardInstructions);
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

                BlockPos targetPos = findTargetEjector(injectorPos, extracted);
                if (targetPos != null) {
                    IItemHandler ejector = ejectors.get(targetPos);
                    int inserted = insertIntoEjector(ejector, extracted);
                    if (inserted > 0) {
                        ItemStack actualExtracted = injector.extractItem(slot, inserted, false);
                        PneumaticItemNetworks.LOGGER.debug("Transferred {} items from injector at {} to ejector at {}", inserted, injectorPos, targetPos);
                    }
                }
            }
        }
    }

    private BlockPos findTargetEjector(BlockPos injectorPos, ItemStack stack) {
        PunchcardData punchcard = punchcardInstructions.get(injectorPos);
        if (punchcard != null) {
            // Use punchcard instructions to determine the target ejector
            // This is a simplified example; you'll need to implement your own logic based on your punchcard format
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    String instruction = punchcard.getInstruction(i, j);
                    if (instruction.startsWith("ROUTE:")) {
                        String[] parts = instruction.split(":");
                        if (parts.length == 3 && stack.getItem().toString().equals(parts[1])) {
                            return new BlockPos(Integer.parseInt(parts[2]), 0, 0); // Simplified; replace with actual coordinate parsing
                        }
                    }
                }
            }
        }

        // If no punchcard or no matching instruction, use default routing logic
        return ejectors.keySet().stream().findFirst().orElse(null);
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

    public void setPunchcardInstructions(BlockPos pos, PunchcardData punchcardData) {
        punchcardInstructions.put(pos, punchcardData);
        PneumaticItemNetworks.LOGGER.debug("Set punchcard instructions at {}", pos);
    }

    public void removePunchcardInstructions(BlockPos pos) {
        punchcardInstructions.remove(pos);
        PneumaticItemNetworks.LOGGER.debug("Removed punchcard instructions at {}", pos);
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