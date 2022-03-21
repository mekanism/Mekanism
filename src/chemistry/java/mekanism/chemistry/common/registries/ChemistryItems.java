package mekanism.chemistry.common.registries;

import mekanism.chemistry.common.MekanismChemistry;
import mekanism.common.registration.impl.ItemDeferredRegister;

public class ChemistryItems {

    private ChemistryItems() {
    }

    public static final ItemDeferredRegister ITEMS = new ItemDeferredRegister(MekanismChemistry.MODID);
}
