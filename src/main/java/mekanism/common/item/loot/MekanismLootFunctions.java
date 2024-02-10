package mekanism.common.item.loot;

import java.util.Set;
import mekanism.common.Mekanism;
import mekanism.common.registration.MekanismDeferredHolder;
import mekanism.common.registration.impl.LootFunctionDeferredRegister;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

//TODO - 1.20.4: Should we remove the fact that some of these extend LootItemConditionalFunction?
public class MekanismLootFunctions {

    public static final LootFunctionDeferredRegister REGISTER = new LootFunctionDeferredRegister(Mekanism.MODID);
    public static final Set<LootContextParam<?>> BLOCK_ENTITY_LOOT_CONTEXT = Set.of(LootContextParams.BLOCK_ENTITY);

    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_ATTACHMENTS = REGISTER.registerCodec("copy_attachments", () -> CopyAttachmentsLootFunction.CODEC);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_CONTAINERS = REGISTER.registerCodec("copy_containers", () -> CopyContainersLootFunction.CODEC);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_FREQUENCIES = REGISTER.registerCodec("copy_frequencies", () -> CopyFrequencyLootFunction.CODEC);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_FILTERS = REGISTER.registerCodec("copy_filters", () -> CopyFiltersLootFunction.CODEC);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_SECURITY = REGISTER.registerCodec("copy_security", () -> CopySecurityLootFunction.CODEC);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_SIDE_CONFIG = REGISTER.registerCodec("copy_side_config", () -> CopySideConfigLootFunction.CODEC);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_TO_ATTACHMENTS = REGISTER.registerCodec("copy_to_attachments", () -> CopyToAttachmentsLootFunction.CODEC);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> COPY_UPGRADES = REGISTER.registerCodec("copy_upgrades", () -> CopyUpgradesLootFunction.CODEC);
    public static final MekanismDeferredHolder<LootItemFunctionType, LootItemFunctionType> PERSONAL_STORAGE = REGISTER.registerBasic("personal_storage_contents", () -> PersonalStorageContentsLootFunction.INSTANCE);
}