package net.darkhax.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.darkhax.tesla.api.implementation.BaseTeslaContainer;
import net.darkhax.tesla.capability.TeslaCapabilities.CapabilityTeslaConsumer;
import net.darkhax.tesla.capability.TeslaCapabilities.CapabilityTeslaHolder;
import net.darkhax.tesla.capability.TeslaCapabilities.CapabilityTeslaProducer;
import net.darkhax.tesla.lib.Constants;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.VERSION_NUMBER, acceptedMinecraftVersions = "[1.9.4,1.10.2]")
public class Tesla {
    
    @Mod.Instance(Constants.MOD_ID)
    public static Tesla instance;
    
    @EventHandler
    public void preInit (FMLPreInitializationEvent event) {
        
        CapabilityManager.INSTANCE.register(ITeslaConsumer.class, new CapabilityTeslaConsumer<ITeslaConsumer>(), BaseTeslaContainer.class);
        CapabilityManager.INSTANCE.register(ITeslaProducer.class, new CapabilityTeslaProducer<ITeslaProducer>(), BaseTeslaContainer.class);
        CapabilityManager.INSTANCE.register(ITeslaHolder.class, new CapabilityTeslaHolder<ITeslaHolder>(), BaseTeslaContainer.class);
    }
}