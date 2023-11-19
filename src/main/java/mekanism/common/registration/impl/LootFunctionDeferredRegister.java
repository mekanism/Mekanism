package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.registration.MekanismDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

@NothingNullByDefault
public class LootFunctionDeferredRegister extends MekanismDeferredRegister<LootItemFunctionType> {

    public LootFunctionDeferredRegister(String modid) {
        super(Registries.LOOT_FUNCTION_TYPE, modid, LootFunctionRegistryObject::new);
    }

    public LootFunctionRegistryObject<LootItemFunctionType> registerBasic(String name, Supplier<LootItemFunction> sup) {
        return register(name, () -> new LootItemFunctionType(Codec.unit(sup.get())));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TYPE extends LootItemFunctionType> LootFunctionRegistryObject<TYPE> register(String name, Supplier<? extends TYPE> sup) {
        return (LootFunctionRegistryObject<TYPE>) super.register(name, sup);
    }
}