package mekanism.common.item.loot;

import java.util.Set;
import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.LootFunctionDeferredRegister;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class MekanismLootFunctions {

    public static final LootFunctionDeferredRegister REGISTER = new LootFunctionDeferredRegister(Mekanism.MODID);
    public static final Set<LootContextParam<?>> BLOCK_ENTITY_LOOT_CONTEXT = Set.of(LootContextParams.BLOCK_ENTITY);

    public static final MekanismDeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<PersonalStorageContentsLootFunction>> PERSONAL_STORAGE = REGISTER.registerBasic("personal_storage_contents", () -> PersonalStorageContentsLootFunction.INSTANCE);
}