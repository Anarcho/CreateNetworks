package net.anarcho.createnetworks.content.network;

import net.anarcho.createnetworks.CreateNetworks;
import net.anarcho.createnetworks.content.items.PunchcardData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PneumaticNetworkManager {
    private static final Map<Level, PneumaticNetworkManager> INSTANCES = new ConcurrentHashMap<>();

    private final Level level;
    private final Map<BlockPos, PneumaticNetwork> networks = new HashMap<>();
    private final Set<PneumaticNetwork> activeNetworks = new HashSet<>();

    private static final int LOG_COOLDOWN = 100; // ticks
    private int ticksSinceLastLog = 0;

    private PneumaticNetworkManager(Level level) {
        this.level = level;
    }

    public static PneumaticNetworkManager get(Level level) {
        return INSTANCES.computeIfAbsent(level, PneumaticNetworkManager::new);
    }

    public void addPipe(BlockPos pos) {
        PneumaticNetwork network = findOrCreateNetwork(pos);
        network.addPipe(pos);
        updateAdjacentConnections(pos, network);
        updateNetworkActivity(network);
        CreateNetworks.LOGGER.debug("Added pipe to network at {}", pos);
    }

    public void removePipe(BlockPos pos) {
        PneumaticNetwork network = networks.remove(pos);
        if (network != null) {
            network.removePipe(pos);
            if (network.isEmpty()) {
                activeNetworks.remove(network);
                CreateNetworks.LOGGER.debug("Removed empty network");
            } else {
                rebuildNetwork(network);
                updateNetworkActivity(network);
            }
            CreateNetworks.LOGGER.debug("Removed pipe from network at {}", pos);
        }
    }

    public void updatePipeConnections(BlockPos currentPos) {
        PneumaticNetwork network = networks.get(currentPos);
        if (network != null) {
            network.updateConnections(level, currentPos);
            updateNetworkActivity(network);
            CreateNetworks.LOGGER.debug("Updated pipe connections at {}", currentPos);
        }
    }

    public void addInjector(BlockPos pos, IItemHandler itemHandler) {
        PneumaticNetwork network = findOrCreateNetwork(pos);
        network.addInjector(pos, itemHandler);
        updateNetworkActivity(network);
        CreateNetworks.LOGGER.debug("Added injector to network at {}", pos);
    }

    public void removeInjector(BlockPos pos) {
        PneumaticNetwork network = networks.get(pos);
        if (network != null) {
            network.removeInjector(pos);
            updateNetworkActivity(network);
            CreateNetworks.LOGGER.debug("Removed injector from network at {}", pos);
        }
    }

    public void addEjector(BlockPos pos, IItemHandler itemHandler) {
        PneumaticNetwork network = findOrCreateNetwork(pos);
        network.addEjector(pos, itemHandler);
        updateNetworkActivity(network);
        CreateNetworks.LOGGER.debug("Added ejector to network at {}", pos);
    }

    public void removeEjector(BlockPos pos) {
        PneumaticNetwork network = networks.get(pos);
        if (network != null) {
            network.removeEjector(pos);
            updateNetworkActivity(network);
            CreateNetworks.LOGGER.debug("Removed ejector from network at {}", pos);
        }
    }

    private PneumaticNetwork findOrCreateNetwork(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            PneumaticNetwork existingNetwork = networks.get(neighborPos);
            if (existingNetwork != null) {
                return existingNetwork;
            }
        }
        PneumaticNetwork newNetwork = new PneumaticNetwork();
        networks.put(pos, newNetwork);
        CreateNetworks.LOGGER.debug("Created new network at {}", pos);
        return newNetwork;
    }

    private void updateAdjacentConnections(BlockPos pos, PneumaticNetwork network) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            PneumaticNetwork neighborNetwork = networks.get(neighborPos);
            if (neighborNetwork != null && neighborNetwork != network) {
                mergeNetworks(network, neighborNetwork);
            }
            networks.put(neighborPos, network);
        }
    }

    public void connectComponents(BlockPos pos1, BlockPos pos2) {
        PneumaticNetwork network1 = networks.get(pos1);
        PneumaticNetwork network2 = networks.get(pos2);

        if (network1 == null && network2 == null) {
            PneumaticNetwork newNetwork = new PneumaticNetwork();
            newNetwork.addPipe(pos1);
            newNetwork.addPipe(pos2);
            networks.put(pos1, newNetwork);
            networks.put(pos2, newNetwork);
            CreateNetworks.LOGGER.debug("Created new network connecting {} and {}", pos1, pos2);
        } else if (network1 == null) {
            network2.addPipe(pos1);
            networks.put(pos1, network2);
            CreateNetworks.LOGGER.debug("Added {} to existing network at {}", pos1, pos2);
        } else if (network2 == null) {
            network1.addPipe(pos2);
            networks.put(pos2, network1);
            CreateNetworks.LOGGER.debug("Added {} to existing network at {}", pos2, pos1);
        } else if (network1 != network2) {
            mergeNetworks(network1, network2);
            CreateNetworks.LOGGER.debug("Merged networks at {} and {}", pos1, pos2);
        }

        updateNetworkActivity(networks.get(pos1));
    }

    private void mergeNetworks(PneumaticNetwork network1, PneumaticNetwork network2) {
        network1.merge(network2);
        for (BlockPos pos : network2.getAllPositions()) {
            networks.put(pos, network1);
        }
        activeNetworks.remove(network2);
    }

    private void rebuildNetwork(PneumaticNetwork oldNetwork) {
        Set<BlockPos> visited = new HashSet<>();
        for (BlockPos pos : oldNetwork.getAllPositions()) {
            if (!visited.contains(pos)) {
                PneumaticNetwork newNetwork = new PneumaticNetwork();
                buildNetworkDFS(pos, newNetwork, visited);
                updateNetworkActivity(newNetwork);
            }
        }
        CreateNetworks.LOGGER.debug("Rebuilt network");
    }

    private void buildNetworkDFS(BlockPos pos, PneumaticNetwork network, Set<BlockPos> visited) {
        visited.add(pos);
        network.addPipe(pos);
        networks.put(pos, network);

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            if (!visited.contains(neighborPos) && networks.containsKey(neighborPos)) {
                buildNetworkDFS(neighborPos, network, visited);
            }
        }
    }

    private void updateNetworkActivity(PneumaticNetwork network) {
        boolean wasActive = activeNetworks.contains(network);
        boolean isActive = network.isActive();
        if (isActive != wasActive) {
            if (isActive) {
                activeNetworks.add(network);
                CreateNetworks.LOGGER.info("Network became active. Injectors: {}, Ejectors: {}",
                        network.getInjectorCount(), network.getEjectorCount());
            } else {
                activeNetworks.remove(network);
                CreateNetworks.LOGGER.info("Network became inactive. Injectors: {}, Ejectors: {}",
                        network.getInjectorCount(), network.getEjectorCount());
            }
        }
    }

    public void tick() {
        int activeNetworkCount = activeNetworks.size();
        if (activeNetworkCount > 0) {
            if (ticksSinceLastLog >= LOG_COOLDOWN) {
                CreateNetworks.LOGGER.debug("Ticking {} active networks", activeNetworkCount);
                ticksSinceLastLog = 0;
            } else {
                ticksSinceLastLog++;
            }
            for (PneumaticNetwork network : activeNetworks) {
                network.tick();
            }
        }
    }

    public void applyPunchcardInstructions(BlockPos pos, PunchcardData punchcardData) {
        PneumaticNetwork network = networks.get(pos);
        if (network != null) {
            network.setPunchcardInstructions(pos, punchcardData);
            CreateNetworks.LOGGER.debug("Applied punchcard instructions to network at {}", pos);
        }
    }

    public void removePunchcardInstructions(BlockPos pos) {
        PneumaticNetwork network = networks.get(pos);
        if (network != null) {
            network.removePunchcardInstructions(pos);
            CreateNetworks.LOGGER.debug("Removed punchcard instructions from network at {}", pos);
        }
    }

    public void clear() {
        networks.clear();
        activeNetworks.clear();
        CreateNetworks.LOGGER.debug("Cleared all networks");
    }

    public static void clearAll() {
        INSTANCES.values().forEach(PneumaticNetworkManager::clear);
        INSTANCES.clear();
        CreateNetworks.LOGGER.debug("Cleared all PneumaticNetworkManager instances");
    }
}