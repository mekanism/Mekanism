package mekanism.common.item.loot;

import java.util.Set;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Loot function which copies a frequency to the stack's attachments
 */
@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class CopyFrequencyLootFunction implements LootItemFunction {

    public static final CopyFrequencyLootFunction INSTANCE = new CopyFrequencyLootFunction();

    private CopyFrequencyLootFunction() {
    }

    @Override
    public LootItemFunctionType getType() {
        return MekanismLootFunctions.COPY_FREQUENCIES.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof IFrequencyHandler frequencyHandler) {
            stack.getData(MekanismAttachmentTypes.FREQUENCY_COMPONENT).copyFrom(frequencyHandler.getFrequencyComponent());
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