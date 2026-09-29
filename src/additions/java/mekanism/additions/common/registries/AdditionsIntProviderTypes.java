package mekanism.additions.common.registries;

import com.mojang.serialization.MapCodec;
import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.world.ScaledIntProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.IntProvider;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AdditionsIntProviderTypes {

    private AdditionsIntProviderTypes() {
    }

    public static final DeferredRegister<MapCodec<? extends IntProvider>> INT_PROVIDER_TYPES = DeferredRegister.create(Registries.INT_PROVIDER_TYPE, MekanismAdditions.MODID);

    public static final DeferredHolder<MapCodec<? extends IntProvider>, MapCodec<ScaledIntProvider>> SCALED = INT_PROVIDER_TYPES.register("scaled", () -> ScaledIntProvider.CODEC);
}