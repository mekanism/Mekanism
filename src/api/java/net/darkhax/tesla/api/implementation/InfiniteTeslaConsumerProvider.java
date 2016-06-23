package net.darkhax.tesla.api.implementation;

import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * A capability provider for the infinite Tesla consumer capability.
 */
public class InfiniteTeslaConsumerProvider implements ICapabilityProvider {
    
    @Override
    public boolean hasCapability (Capability<?> capability, EnumFacing facing) {
        
        return capability == TeslaCapabilities.CAPABILITY_CONSUMER;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability (Capability<T> capability, EnumFacing facing) {
        
        return capability == TeslaCapabilities.CAPABILITY_CONSUMER ? (T) new InfiniteTeslaConsumer() : null;
    }
}
