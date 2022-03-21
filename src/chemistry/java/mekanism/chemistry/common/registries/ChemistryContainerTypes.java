package mekanism.chemistry.common.registries;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.common.registration.impl.ContainerTypeDeferredRegister;

public class ChemistryContainerTypes {

    private ChemistryContainerTypes() {
    }

    public static final ContainerTypeDeferredRegister CONTAINER_TYPES = new ContainerTypeDeferredRegister(MekanismChemistry.MODID);
}
