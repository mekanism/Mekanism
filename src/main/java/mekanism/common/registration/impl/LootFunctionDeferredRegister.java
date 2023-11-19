package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

@NothingNullByDefault
public class LootFunctionDeferredRegister extends MekanismDeferredRegister<LootItemFunctionType> {

    public LootFunctionDeferredRegister(String modid) {
        super(Registries.LOOT_FUNCTION_TYPE, modid);
    }

    public MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> registerBasic(String name, Supplier<LootItemFunction> sup) {
        return register(name, () -> new LootItemFunctionType(Codec.unit(sup.get())));
    }
}