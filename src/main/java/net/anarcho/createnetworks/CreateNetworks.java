package net.anarcho.createnetworks;

import net.anarcho.createnetworks.registry.Registration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CreateNetworks.MOD_ID)
public class CreateNetworks {
    public static final String MOD_ID = "createnetworks";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public CreateNetworks() {
        Registration.init();
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Pneumatic Item Networks mod initialized");
    }
}
