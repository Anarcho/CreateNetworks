package net.anarcho.createnetworks.event;

import net.anarcho.createnetworks.CreateNetworks;
import net.anarcho.createnetworks.content.network.PneumaticNetworkManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateNetworks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEventHandler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for (var level : event.getServer().getAllLevels()) {
                PneumaticNetworkManager.get(level).tick();
            }
        }
    }
}