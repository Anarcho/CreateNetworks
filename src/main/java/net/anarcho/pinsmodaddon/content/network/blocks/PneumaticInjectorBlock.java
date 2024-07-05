package net.anarcho.pinsmodaddon.content.network.blocks;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.anarcho.pinsmodaddon.content.network.blockentities.PneumaticInjectorBlockEntity;
import net.anarcho.pinsmodaddon.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PneumaticInjectorBlock extends DirectionalKineticBlock implements IBE<PneumaticInjectorBlockEntity> {

    public PneumaticInjectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y; // Allow rotation around Y-axis
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return true; // Allow shaft connection from any side
    }

    @Override
    public Class<PneumaticInjectorBlockEntity> getBlockEntityClass() {
        return PneumaticInjectorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PneumaticInjectorBlockEntity> getBlockEntityType() {
        return ModBlockEntities.PNEUMATIC_INJECTOR.get();
    }
}