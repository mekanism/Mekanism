package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.world.DisableableFeaturePlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MekanismPlacementModifiers {

    private MekanismPlacementModifiers() {
    }

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Mekanism.MODID);

    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<DisableableFeaturePlacement>> DISABLEABLE = PLACEMENT_MODIFIERS.register("disableable", () -> () -> DisableableFeaturePlacement.CODEC);
}