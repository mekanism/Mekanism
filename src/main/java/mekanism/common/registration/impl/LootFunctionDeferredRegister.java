package mekanism.common.registration.impl;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

@NothingNullByDefault
public class LootFunctionDeferredRegister extends MekanismDeferredRegister<LootItemFunctionType<?>> {

    public LootFunctionDeferredRegister(String modid) {
        super(Registries.LOOT_FUNCTION_TYPE, modid);
    }

    public <T extends LootItemFunction> MekanismDeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<T>> registerBasic(String name, Supplier<T> sup) {
        return registerCodec(name, () -> MapCodec.unit(sup.get()));
    }

    public <T extends LootItemFunction> MekanismDeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<T>> registerCodec(String name, Supplier<MapCodec<T>> sup) {
        return register(name, () -> new LootItemFunctionType<>(sup.get()));
    }
}