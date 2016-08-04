package net.darkhax.tesla.capability;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class TeslaCapabilities {
    
    /**
     * Access to the consumer capability. Can be used for making checks.
     */
    @CapabilityInject(ITeslaConsumer.class)
    public static Capability<ITeslaConsumer> CAPABILITY_CONSUMER = null;
    
    /**
     * Access to the producer capability. Can be used for making checks.
     */
    @CapabilityInject(ITeslaProducer.class)
    public static Capability<ITeslaProducer> CAPABILITY_PRODUCER = null;
    
    /**
     * Access to the holder capability. Can be used for making checks.
     */
    @CapabilityInject(ITeslaHolder.class)
    public static Capability<ITeslaHolder> CAPABILITY_HOLDER = null;
    
    public static class CapabilityTeslaConsumer<T extends ITeslaConsumer> implements IStorage<ITeslaConsumer> {
        
        @Override
        public NBTBase writeNBT (Capability<ITeslaConsumer> capability, ITeslaConsumer instance, EnumFacing side) {
            
            return null;
        }
        
        @Override
        public void readNBT (Capability<ITeslaConsumer> capability, ITeslaConsumer instance, EnumFacing side, NBTBase nbt) {
        
        }
    }
    
    public static class CapabilityTeslaProducer<T extends ITeslaProducer> implements IStorage<ITeslaProducer> {
        
        @Override
        public NBTBase writeNBT (Capability<ITeslaProducer> capability, ITeslaProducer instance, EnumFacing side) {
            
            return null;
        }
        
        @Override
        public void readNBT (Capability<ITeslaProducer> capability, ITeslaProducer instance, EnumFacing side, NBTBase nbt) {
        
        }
    }
    
    public static class CapabilityTeslaHolder<T extends ITeslaHolder> implements IStorage<ITeslaHolder> {
        
        @Override
        public NBTBase writeNBT (Capability<ITeslaHolder> capability, ITeslaHolder instance, EnumFacing side) {
            
            return null;
        }
        
        @Override
        public void readNBT (Capability<ITeslaHolder> capability, ITeslaHolder instance, EnumFacing side, NBTBase nbt) {
        
        }
    }
}