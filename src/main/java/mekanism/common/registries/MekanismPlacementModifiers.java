package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import mekanism.common.world.DisableableFeaturePlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class MekanismPlacementModifiers {

    private MekanismPlacementModifiers() {
    }

    public static final MekanismDeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = new MekanismDeferredRegister<>(Registries.PLACEMENT_MODIFIER_TYPE, Mekanism.MODID);

    public static final MekanismDeferredHolder<PlacementModifierType<?>, PlacementModifierType<DisableableFeaturePlacement>> DISABLEABLE = PLACEMENT_MODIFIERS.register("disableable", () -> () -> DisableableFeaturePlacement.CODEC);
}