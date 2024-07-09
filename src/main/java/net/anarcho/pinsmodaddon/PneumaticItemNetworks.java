package net.anarcho.pinsmodaddon;

import net.anarcho.pinsmodaddon.registry.Registration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PneumaticItemNetworks.MOD_ID)
public class PneumaticItemNetworks {
    public static final String MOD_ID = "pinsmodaddon";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public PneumaticItemNetworks() {
        Registration.init();
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Pneumatic Item Networks mod initialized");
    }
}
