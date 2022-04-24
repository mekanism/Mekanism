package mekanism.common.registration.impl;

import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.registries.RegistryObject;

public class PlacementModifierRegistryObject<PROVIDER extends PlacementModifier> extends WrappedRegistryObject<PlacementModifierType<PROVIDER>> {

    public PlacementModifierRegistryObject(RegistryObject<PlacementModifierType<PROVIDER>> registryObject) {
        super(registryObject);
    }
}