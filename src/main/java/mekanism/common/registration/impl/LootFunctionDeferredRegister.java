package mekanism.common.registration.impl;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class LootFunctionDeferredRegister extends WrappedDeferredRegister<LootItemFunctionType> {

    public LootFunctionDeferredRegister(String modid) {
        super(modid, Registries.LOOT_FUNCTION_TYPE);
    }

    public LootFunctionRegistryObject<LootItemFunctionType> registerBasic(String name, Supplier<LootItemFunction> sup) {
        return register(name, () -> new LootItemFunctionType(Codec.unit(sup.get())));
    }

    public <TYPE extends LootItemFunctionType> LootFunctionRegistryObject<TYPE> register(String name, Supplier<TYPE> sup) {
        return register(name, sup, LootFunctionRegistryObject::new);
    }
}