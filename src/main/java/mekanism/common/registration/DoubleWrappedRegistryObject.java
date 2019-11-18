package mekanism.common.registration;

import javax.annotation.Nullable;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class DoubleWrappedRegistryObject<PRIMARY extends IForgeRegistryEntry<? super PRIMARY>, SECONDARY extends IForgeRegistryEntry<? super SECONDARY>> implements INamedEntry {

    private final RegistryObject<PRIMARY> primaryRegistryObject;
    private final RegistryObject<SECONDARY> secondaryRegistryObject;

    public DoubleWrappedRegistryObject(RegistryObject<PRIMARY> primaryRegistryObject, RegistryObject<SECONDARY> secondaryRegistryObject) {
        this.primaryRegistryObject = primaryRegistryObject;
        this.secondaryRegistryObject = secondaryRegistryObject;
    }

    //TODO: Should this be nullable?? the registryObject.get is. We should handle the fact that extenders of this previously thought it is nonnull
    @Nullable
    public PRIMARY getPrimary() {
        return primaryRegistryObject.get();
    }

    @Nullable
    public SECONDARY getSecondary() {
        return secondaryRegistryObject.get();
    }

    public RegistryObject<PRIMARY> getPrimaryInternal() {
        return primaryRegistryObject;
    }

    public RegistryObject<SECONDARY> getSecondaryInternal() {
        return secondaryRegistryObject;
    }

    @Override
    public String getInternalRegistryName() {
        return primaryRegistryObject.getId().getPath();
    }
}