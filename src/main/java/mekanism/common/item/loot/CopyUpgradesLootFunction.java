package mekanism.common.item.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.tile.interfaces.IUpgradeTile;
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
 * Loot function which copies security data to the stack's attachments
 */
@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class CopyUpgradesLootFunction extends LootItemConditionalFunction {

    private static final Set<LootContextParam<?>> REFERENCED_PARAMS = Set.of(LootContextParams.BLOCK_ENTITY);
    public static final Codec<CopyUpgradesLootFunction> CODEC = RecordCodecBuilder.create(instance -> commonFields(instance)
          .apply(instance, CopyUpgradesLootFunction::new)
    );

    private CopyUpgradesLootFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType getType() {
        return MekanismLootFunctions.COPY_UPGRADES.get();
    }

    @Override
    public ItemStack run(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof IUpgradeTile tile) {
            stack.getData(MekanismAttachmentTypes.UPGRADES).deserializeNBT(tile.getComponent().writeUpgradeNbt());
        }
        return stack;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return REFERENCED_PARAMS;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<CopyUpgradesLootFunction.Builder> {

        protected Builder() {
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyUpgradesLootFunction(this.getConditions());
        }
    }
}