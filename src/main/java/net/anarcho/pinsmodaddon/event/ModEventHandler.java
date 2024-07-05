package net.anarcho.pinsmodaddon.event;

import net.anarcho.pinsmodaddon.PneumaticItemNetworks;
import net.anarcho.pinsmodaddon.content.network.PneumaticNetworkManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PneumaticItemNetworks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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