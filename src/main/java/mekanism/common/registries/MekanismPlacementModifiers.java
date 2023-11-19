package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.PlacementModifierDeferredRegister;
import mekanism.common.world.DisableableFeaturePlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class MekanismPlacementModifiers {

    private MekanismPlacementModifiers() {
    }

    public static final PlacementModifierDeferredRegister PLACEMENT_MODIFIERS = new PlacementModifierDeferredRegister(Mekanism.MODID);

    public static final MekanismDeferredHolder<PlacementModifierType<?>, PlacementModifierType<DisableableFeaturePlacement>> DISABLEABLE = PLACEMENT_MODIFIERS.register("disableable", DisableableFeaturePlacement.CODEC);
}