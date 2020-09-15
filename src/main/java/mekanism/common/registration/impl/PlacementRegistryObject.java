package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.RegistryObject;

public class PlacementRegistryObject<CONFIG extends IPlacementConfig, PLACEMENT extends Placement<CONFIG>> extends WrappedRegistryObject<PLACEMENT> {

    public PlacementRegistryObject(RegistryObject<PLACEMENT> registryObject) {
        super(registryObject);
    }

    @Nonnull
    public PLACEMENT getPlacement() {
        return get();
    }

    @Nonnull
    public ConfiguredPlacement<CONFIG> getConfigured(CONFIG placementConfig) {
        return getPlacement().configure(placementConfig);
    }
}