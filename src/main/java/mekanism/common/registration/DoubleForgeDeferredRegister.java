package mekanism.common.registration;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class DoubleForgeDeferredRegister<PRIMARY extends IForgeRegistryEntry<PRIMARY>, SECONDARY extends IForgeRegistryEntry<SECONDARY>>
      extends DoubleDeferredRegister<PRIMARY, SECONDARY> {

    public DoubleForgeDeferredRegister(String modid, IForgeRegistry<PRIMARY> primaryRegistry, IForgeRegistry<SECONDARY> secondaryRegistry) {
        super(DeferredRegister.create(primaryRegistry, modid), DeferredRegister.create(secondaryRegistry, modid));
    }
}