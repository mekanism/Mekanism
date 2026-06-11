package mekanism.common.registration.impl;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStackTemplate;
import mekanism.common.registration.DoubleWrappedRegistryObject;
import net.neoforged.neoforge.registries.DeferredHolder;

public class SlurryRegistryObject<DIRTY extends Chemical, CLEAN extends Chemical> extends DoubleWrappedRegistryObject<Chemical, DIRTY, Chemical, CLEAN> {

    public SlurryRegistryObject(DeferredChemical<DIRTY> dirtyRO, DeferredChemical<CLEAN> cleanRO) {
        super(dirtyRO, cleanRO);
    }

    public DeferredHolder<Chemical, CLEAN> getCleanSlurry() {
        return secondaryRO;
    }

    public ChemicalStackTemplate asDirtyTemplate(int size) {
        return new ChemicalStackTemplate(this, size);
    }

    public ChemicalStackTemplate asCleanTemplate(int size) {
        return new ChemicalStackTemplate(secondaryRO, size);
    }
}