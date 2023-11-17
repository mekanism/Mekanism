package mekanism.common.item.loot;

import mekanism.common.Mekanism;
import mekanism.common.registration.impl.LootFunctionDeferredRegister;
import mekanism.common.registration.impl.LootFunctionRegistryObject;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class MekanismLootFunctions {

    public static final LootFunctionDeferredRegister REGISTER = new LootFunctionDeferredRegister(Mekanism.MODID);

    public static final LootFunctionRegistryObject<LootItemFunctionType> PERSONAL_STORAGE_LOOT_FUNC = REGISTER.registerBasic("personal_storage_contents", () -> PersonalStorageContentsLootFunction.INSTANCE);
}