package mekanism.common.item.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Loot function which copies filter data to the stack's attachments
 */
@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class CopyFiltersLootFunction extends LootItemConditionalFunction {

    public static final Codec<CopyFiltersLootFunction> CODEC = RecordCodecBuilder.create(instance -> commonFields(instance)
          .apply(instance, CopyFiltersLootFunction::new)
    );

    private CopyFiltersLootFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType getType() {
        return MekanismLootFunctions.COPY_FILTERS.get();
    }

    @Override
    public ItemStack run(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof ITileFilterHolder<?> tile) {
            stack.getData(MekanismAttachmentTypes.FILTER_AWARE).copyFrom(tile.getFilterManager());
        }
        return stack;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return MekanismLootFunctions.BLOCK_ENTITY_LOOT_CONTEXT;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<CopyFiltersLootFunction.Builder> {

        protected Builder() {
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyFiltersLootFunction(getConditions());
        }
    }
}