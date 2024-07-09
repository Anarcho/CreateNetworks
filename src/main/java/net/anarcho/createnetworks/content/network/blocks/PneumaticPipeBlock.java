package net.anarcho.createnetworks.content.network.blocks;

import net.anarcho.createnetworks.CreateNetworks;
import net.anarcho.createnetworks.content.network.PneumaticNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class PneumaticPipeBlock extends Block {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    protected static final VoxelShape SHAPE = Block.box(4, 4, 4, 12, 12, 12);

    public PneumaticPipeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return updateConnections(context.getLevel(), context.getClickedPos(), defaultBlockState());
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(this) && !level.isClientSide()) {
            level.setBlock(pos, updateConnections(level, pos, state), 3);
            PneumaticNetworkManager.get(level).addPipe(pos);
            CreateNetworks.LOGGER.debug("Placed PneumaticPipe at {}. Updating connections.", pos);
        }
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!newState.is(this) && !level.isClientSide()) {
            PneumaticNetworkManager.get(level).removePipe(pos);
            CreateNetworks.LOGGER.debug("Removed PneumaticPipe at {}.", pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState stateIn, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        if (level instanceof Level && !level.isClientSide()) {
            return updateConnections((Level) level, currentPos, stateIn);
        }
        return stateIn;
    }

    private BlockState updateConnections(Level level, BlockPos pos, BlockState currentState) {
        BlockState newState = currentState;
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            boolean shouldConnect = canConnect(neighborState, direction);
            newState = newState.setValue(getPropertyForDirection(direction), shouldConnect);

            if (shouldConnect) {
                PneumaticNetworkManager.get(level).connectComponents(pos, neighborPos);
                CreateNetworks.LOGGER.debug("Connected {} at {} to {} at {} in direction {}",
                        this.getClass().getSimpleName(), pos, neighborState.getBlock().getClass().getSimpleName(), neighborPos, direction);
            }
        }
        return newState;
    }

    private boolean canConnect(BlockState state, Direction direction) {
        return state.getBlock() instanceof PneumaticPipeBlock
                || state.getBlock() instanceof PneumaticInjectorBlock
                || state.getBlock() instanceof PneumaticEjectorBlock;
    }

    public static BooleanProperty getPropertyForDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }
}