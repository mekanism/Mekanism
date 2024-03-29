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

    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_ATTACHMENTS = REGISTER.registerCodec("copy_attachments", () -> CopyAttachmentsLootFunction.CODEC);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_CONTAINERS = REGISTER.registerCodec("copy_containers", () -> CopyContainersLootFunction.CODEC);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_FREQUENCIES = REGISTER.registerBasic("copy_frequencies", () -> CopyFrequencyLootFunction.INSTANCE);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_FILTERS = REGISTER.registerBasic("copy_filters", () -> CopyFiltersLootFunction.INSTANCE);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_SECURITY = REGISTER.registerBasic("copy_security", () -> CopySecurityLootFunction.INSTANCE);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_SIDE_CONFIG = REGISTER.registerBasic("copy_side_config", () -> CopySideConfigLootFunction.INSTANCE);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_TO_ATTACHMENTS = REGISTER.registerCodec("copy_to_attachments", () -> CopyToAttachmentsLootFunction.CODEC);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_UPGRADES = REGISTER.registerBasic("copy_upgrades", () -> CopyUpgradesLootFunction.INSTANCE);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> PERSONAL_STORAGE = REGISTER.registerBasic("personal_storage_contents", () -> PersonalStorageContentsLootFunction.INSTANCE);
}