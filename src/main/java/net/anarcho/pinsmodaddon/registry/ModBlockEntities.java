package net.anarcho.pinsmodaddon.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.anarcho.pinsmodaddon.content.network.blockentities.PneumaticInjectorBlockEntity;
import net.anarcho.pinsmodaddon.content.network.blockentities.PneumaticEjectorBlockEntity;
import net.anarcho.pinsmodaddon.content.blockentities.PunchcardProgrammerBlockEntity;

import static net.anarcho.pinsmodaddon.registry.Registration.REGISTRATE;

public class ModBlockEntities {
    public static final BlockEntityEntry<PneumaticInjectorBlockEntity> PNEUMATIC_INJECTOR =
            REGISTRATE.blockEntity("pneumatic_injector", PneumaticInjectorBlockEntity::new)
                    .validBlocks(ModBlocks.PNEUMATIC_INJECTOR)
                    .register();

    public static final BlockEntityEntry<PneumaticEjectorBlockEntity> PNEUMATIC_EJECTOR =
            REGISTRATE.blockEntity("pneumatic_ejector", PneumaticEjectorBlockEntity::new)
                    .validBlocks(ModBlocks.PNEUMATIC_EJECTOR)
                    .register();

    public static final BlockEntityEntry<PunchcardProgrammerBlockEntity> PUNCHCARD_PROGRAMMER =
            REGISTRATE.blockEntity("punchcard_programmer", PunchcardProgrammerBlockEntity::new)
                    .validBlocks(ModBlocks.PUNCHCARD_PROGRAMMER)
                    .register();

    public static void register() {}
}