package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class PlacementModifierDeferredRegister extends MekanismDeferredRegister<PlacementModifierType<?>> {

    public PlacementModifierDeferredRegister(String modid) {
        super(Registries.PLACEMENT_MODIFIER_TYPE, modid);
    }

    public <MODIFIER extends PlacementModifier> MekanismDeferredHolder<PlacementModifierType<?>, PlacementModifierType<MODIFIER>> register(String name, Codec<MODIFIER> codec) {
        return register(name, () -> () -> codec);
    }
}