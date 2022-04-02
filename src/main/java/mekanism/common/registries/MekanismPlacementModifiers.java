package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.PlacementModifierDeferredRegister;
import mekanism.common.registration.impl.PlacementModifierRegistryObject;
import mekanism.common.world.DisableableFeaturePlacement;

public class MekanismPlacementModifiers {

    private MekanismPlacementModifiers() {
    }

    public static final PlacementModifierDeferredRegister PLACEMENT_MODIFIERS = new PlacementModifierDeferredRegister(Mekanism.MODID);

    public static final PlacementModifierRegistryObject<DisableableFeaturePlacement> DISABLEABLE = PLACEMENT_MODIFIERS.register("disableable", DisableableFeaturePlacement.CODEC);
}