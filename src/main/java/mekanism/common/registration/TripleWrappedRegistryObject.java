package mekanism.common.registration;

import mekanism.api.annotations.FieldsAreNonnullByDefault;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public class TripleWrappedRegistryObject<PRIMARY extends IForgeRegistryEntry<? super PRIMARY>, SECONDARY extends IForgeRegistryEntry<? super SECONDARY>, TERTIARY extends IForgeRegistryEntry<? super TERTIARY>> implements INamedEntry
{

    private final RegistryObject<PRIMARY> primaryRO;
    private final RegistryObject<SECONDARY> secondaryRO;
    private final RegistryObject<TERTIARY> tertiaryRO;

    public TripleWrappedRegistryObject(RegistryObject<PRIMARY> primaryRO, RegistryObject<SECONDARY> secondaryRO, RegistryObject<TERTIARY> tertiaryRO)
    {
        this.primaryRO = primaryRO;
        this.secondaryRO = secondaryRO;
        this.tertiaryRO = tertiaryRO;
    }

    @Nonnull
    public PRIMARY getPrimary()
    {
        return primaryRO.get();
    }

    @Nonnull
    public SECONDARY getSecondary()
    {
        return secondaryRO.get();
    }

    @Nonnull
    public TERTIARY getTertiary()
    {
        return tertiaryRO.get();
    }

    @Override
    public String getInternalRegistryName()
    {
        return primaryRO.getId().getPath();
    }
}