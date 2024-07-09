package net.anarcho.pinsmodaddon.registry;

import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.anarcho.pinsmodaddon.PneumaticItemNetworks;
import net.anarcho.pinsmodaddon.content.network.blocks.PneumaticPipeBlock;
import net.anarcho.pinsmodaddon.content.network.blocks.PneumaticInjectorBlock;
import net.anarcho.pinsmodaddon.content.network.blocks.PneumaticEjectorBlock;
import net.anarcho.pinsmodaddon.content.blocks.PunchcardProgrammerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static net.anarcho.pinsmodaddon.registry.Registration.REGISTRATE;

public class ModBlocks {
    public static final BlockEntry<PneumaticPipeBlock> PNEUMATIC_PIPE = REGISTRATE.block("pneumatic_pipe", PneumaticPipeBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .simpleItem()
            .register();

    public static final BlockEntry<PneumaticInjectorBlock> PNEUMATIC_INJECTOR =
            REGISTRATE.block("pneumatic_injector", PneumaticInjectorBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .transform(BlockStressDefaults.setImpact(4.0))
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<PneumaticEjectorBlock> PNEUMATIC_EJECTOR =
            REGISTRATE.block("pneumatic_ejector", PneumaticEjectorBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
                    .transform(BlockStressDefaults.setImpact(3.0))
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<PunchcardProgrammerBlock> PUNCHCARD_PROGRAMMER =
            REGISTRATE.block("punchcard_programmer", PunchcardProgrammerBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .item()
                    .transform(customItemModel())
                    .register();

    public static void register() {
        PneumaticItemNetworks.LOGGER.info("Registering ModBlocks");
    }
}