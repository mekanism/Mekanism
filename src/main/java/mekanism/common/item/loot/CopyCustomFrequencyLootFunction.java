package mekanism.common.item.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyHandler;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registries.MekanismAttachmentTypes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.ExtraCodecs;
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
 * Loot function which copies a custom frequency to the stack's attachments
 */
@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class CopyCustomFrequencyLootFunction extends LootItemConditionalFunction {

    private static final Set<LootContextParam<?>> REFERENCED_PARAMS = Set.of(LootContextParams.BLOCK_ENTITY);
    public static final Codec<CopyCustomFrequencyLootFunction> CODEC = RecordCodecBuilder.create(instance -> commonFields(instance)
          .and(ExtraCodecs.validate(FrequencyType.CODEC,
                type -> type == FrequencyType.SECURITY ? DataResult.error(() -> "Cannot copy security frequency") : DataResult.success(type)
          ).fieldOf("type").forGetter(function -> function.frequencyType))
          .apply(instance, CopyCustomFrequencyLootFunction::new)
    );

    private final FrequencyType<?> frequencyType;

    private CopyCustomFrequencyLootFunction(List<LootItemCondition> conditions, FrequencyType<?> frequencyType) {
        super(conditions);
        this.frequencyType = frequencyType;
    }

    @Override
    public LootItemFunctionType getType() {
        return MekanismLootFunctions.COPY_CUSTOM_FREQUENCY.get();
    }

    @Override
    public ItemStack run(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof IFrequencyHandler frequencyHandler && stack.getItem() instanceof IFrequencyItem frequencyItem && frequencyItem.getFrequencyType() == frequencyType) {
            stack.getData(MekanismAttachmentTypes.FREQUENCY_AWARE).copyFromComponent(frequencyHandler.getFrequencyComponent());
        }
        return stack;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return REFERENCED_PARAMS;
    }

    public static Builder builder(FrequencyType<?> frequencyType) {
        return new Builder(frequencyType);
    }

    public static class Builder extends LootItemConditionalFunction.Builder<CopyCustomFrequencyLootFunction.Builder> {

        private final FrequencyType<?> frequencyType;

        protected Builder(FrequencyType<?> frequencyType) {
            this.frequencyType = frequencyType;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyCustomFrequencyLootFunction(this.getConditions(), this.frequencyType);
        }
    }
}