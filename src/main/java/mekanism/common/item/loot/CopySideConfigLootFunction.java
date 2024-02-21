package mekanism.common.item.loot;

import java.util.Set;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Loot function which copies security data to the stack's attachments
 */
@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class CopySideConfigLootFunction implements LootItemFunction {

    public static final CopySideConfigLootFunction INSTANCE = new CopySideConfigLootFunction();

    private CopySideConfigLootFunction() {
    }

    @Override
    public LootItemFunctionType getType() {
        return MekanismLootFunctions.COPY_SIDE_CONFIG.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof ISideConfiguration tile) {
            stack.getData(MekanismAttachmentTypes.SIDE_CONFIG).copyFrom(tile.getConfig());
            stack.getData(MekanismAttachmentTypes.EJECTOR).copyFrom(tile.getEjector());
        }
        return stack;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return MekanismLootFunctions.BLOCK_ENTITY_LOOT_CONTEXT;
    }

    public static LootItemFunction.Builder builder() {
        return () -> INSTANCE;
    }
}