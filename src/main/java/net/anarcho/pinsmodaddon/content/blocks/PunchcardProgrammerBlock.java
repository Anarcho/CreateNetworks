package net.anarcho.pinsmodaddon.content.blocks;

import com.simibubi.create.foundation.block.IBE;
import net.anarcho.pinsmodaddon.content.blockentities.PunchcardProgrammerBlockEntity;
import net.anarcho.pinsmodaddon.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class PunchcardProgrammerBlock extends Block implements IBE<PunchcardProgrammerBlockEntity> {

    public PunchcardProgrammerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<PunchcardProgrammerBlockEntity> getBlockEntityClass() {
        return PunchcardProgrammerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PunchcardProgrammerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.PUNCHCARD_PROGRAMMER.get();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            PunchcardProgrammerBlockEntity be = getBlockEntity(level, pos);
            if (be != null) {
                NetworkHooks.openScreen(serverPlayer, be, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}