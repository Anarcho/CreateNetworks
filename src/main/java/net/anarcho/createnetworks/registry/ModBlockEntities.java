package net.anarcho.createnetworks.registry;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.anarcho.createnetworks.content.network.blockentities.PneumaticInjectorBlockEntity;
import net.anarcho.createnetworks.content.network.blockentities.PneumaticEjectorBlockEntity;
import net.anarcho.createnetworks.content.blockentities.PunchcardProgrammerBlockEntity;

import static net.anarcho.createnetworks.registry.Registration.REGISTRATE;

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