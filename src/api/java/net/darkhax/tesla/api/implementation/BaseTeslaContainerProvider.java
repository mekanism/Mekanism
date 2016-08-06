package net.darkhax.tesla.api.implementation;

import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * A capability provider for the base Tesla container implementation.
 */
public class BaseTeslaContainerProvider implements INBTSerializable<NBTTagCompound>, ICapabilityProvider {
    
    /**
     * The capability being provided.
     */
    private final BaseTeslaContainer container;
    
    /**
     * Constructor for setting the BaseTeslaContainer for the provider to provide.
     * 
     * @param container The BaseTeslaContainer to provide.
     */
    public BaseTeslaContainerProvider(BaseTeslaContainer container) {
        
        this.container = container;
    }
    
    @Override
    public boolean hasCapability (Capability<?> capability, EnumFacing facing) {
        
        return capability == TeslaCapabilities.CAPABILITY_CONSUMER || capability == TeslaCapabilities.CAPABILITY_PRODUCER || capability == TeslaCapabilities.CAPABILITY_HOLDER;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability (Capability<T> capability, EnumFacing facing) {
        
        if (capability == TeslaCapabilities.CAPABILITY_CONSUMER || capability == TeslaCapabilities.CAPABILITY_PRODUCER || capability == TeslaCapabilities.CAPABILITY_HOLDER)
            return (T) this.container;
            
        return null;
    }
    
    @Override
    public NBTTagCompound serializeNBT () {
        
        return this.container.serializeNBT();
    }
    
    @Override
    public void deserializeNBT (NBTTagCompound nbt) {
        
        this.container.deserializeNBT(nbt);
    }
}