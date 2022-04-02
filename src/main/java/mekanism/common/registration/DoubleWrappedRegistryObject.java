package mekanism.common.registration;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import net.minecraftforge.registries.RegistryObject;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class DoubleWrappedRegistryObject<PRIMARY, SECONDARY> implements INamedEntry {

    protected final RegistryObject<PRIMARY> primaryRO;
    protected final RegistryObject<SECONDARY> secondaryRO;

    public DoubleWrappedRegistryObject(RegistryObject<PRIMARY> primaryRO, RegistryObject<SECONDARY> secondaryRO) {
        this.primaryRO = primaryRO;
        this.secondaryRO = secondaryRO;
    }

    @Nonnull
    public PRIMARY getPrimary() {
        return primaryRO.get();
    }

    @Nonnull
    public SECONDARY getSecondary() {
        return secondaryRO.get();
    }

    @Override
    public String getInternalRegistryName() {
        return primaryRO.getId().getPath();
    }
}