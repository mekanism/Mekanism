package mekanism.common.registration;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraftforge.registries.RegistryObject;

@NothingNullByDefault
public class DoubleWrappedRegistryObject<PRIMARY, SECONDARY> implements INamedEntry {

    protected final RegistryObject<PRIMARY> primaryRO;
    protected final RegistryObject<SECONDARY> secondaryRO;

    public DoubleWrappedRegistryObject(RegistryObject<PRIMARY> primaryRO, RegistryObject<SECONDARY> secondaryRO) {
        this.primaryRO = primaryRO;
        this.secondaryRO = secondaryRO;
    }

    public PRIMARY getPrimary() {
        return primaryRO.get();
    }

    public SECONDARY getSecondary() {
        return secondaryRO.get();
    }

    @Override
    public String getInternalRegistryName() {
        return primaryRO.getId().getPath();
    }
}