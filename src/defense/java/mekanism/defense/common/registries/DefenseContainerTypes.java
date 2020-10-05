package mekanism.defense.common.registries;

import mekanism.common.registration.impl.ContainerTypeDeferredRegister;
import mekanism.defense.common.MekanismDefense;

public class DefenseContainerTypes {

    private DefenseContainerTypes() {
    }

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(MekanismDefense.MODID);
}