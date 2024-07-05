package net.anarcho.pinsmodaddon.registry;

import net.anarcho.pinsmodaddon.PneumaticItemNetworks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PneumaticItemNetworks.MOD_ID);

    public static final RegistryObject<CreativeModeTab> PNEUMATIC_NETWORKS_TAB = CREATIVE_MODE_TABS.register("pneumatic_networks_tab", () ->
            CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.PNEUMATIC_PIPE.get()))
                    .title(Component.translatable("itemGroup." + PneumaticItemNetworks.MOD_ID + ".pneumatic_networks_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.PNEUMATIC_PIPE.get());
                        output.accept(ModBlocks.PNEUMATIC_INJECTOR.get());
                        output.accept(ModBlocks.PNEUMATIC_EJECTOR.get());
                        // Add more items here as needed
                    })
                    .build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}