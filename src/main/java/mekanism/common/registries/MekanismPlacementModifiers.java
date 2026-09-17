package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.registration.DeferredMapCodecHolder;
import mekanism.common.registration.DeferredMapCodecRegister;
import mekanism.common.world.DisableableFeaturePlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class MekanismPlacementModifiers {

    private MekanismPlacementModifiers() {
    }

    public static final DeferredMapCodecRegister<PlacementModifier> PLACEMENT_MODIFIERS = new DeferredMapCodecRegister<>(Registries.PLACEMENT_MODIFIER_TYPE, Mekanism.MODID);

    public static final DeferredMapCodecHolder<PlacementModifier, DisableableFeaturePlacement> DISABLEABLE = PLACEMENT_MODIFIERS.registerCodec("disableable", () -> DisableableFeaturePlacement.CODEC);
}