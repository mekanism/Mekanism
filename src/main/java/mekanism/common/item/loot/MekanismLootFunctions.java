package mekanism.common.item.loot;

import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.LootFunctionDeferredRegister;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class MekanismLootFunctions {

    public static final LootFunctionDeferredRegister REGISTER = new LootFunctionDeferredRegister(Mekanism.MODID);

    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> PERSONAL_STORAGE = REGISTER.registerBasic("personal_storage_contents", () -> PersonalStorageContentsLootFunction.INSTANCE);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_CONTAINERS = REGISTER.registerCodec("copy_containers", () -> CopyContainersLootFunction.CODEC);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_SECURITY = REGISTER.registerCodec("copy_security", () -> CopySecurityLootFunction.CODEC);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_CUSTOM_FREQUENCY = REGISTER.registerCodec("copy_custom_frequency", () -> CopyCustomFrequencyLootFunction.CODEC);
}