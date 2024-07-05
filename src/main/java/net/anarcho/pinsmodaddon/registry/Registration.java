package net.anarcho.pinsmodaddon.registry;

import com.tterrag.registrate.Registrate;
import net.anarcho.pinsmodaddon.PneumaticItemNetworks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class Registration {
    public static final Registrate REGISTRATE = Registrate.create(PneumaticItemNetworks.MOD_ID);

    public static void init() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register blocks, items, and block entities
        ModBlocks.register();
        ModBlockEntities.register();

        // Register the creative tab
        ModCreativeTabs.register(modEventBus);
    }
}