package mekanism.common.item.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.attachments.security.SecurityObject;
import mekanism.common.lib.security.ISecurityTile;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.tile.component.TileComponentSecurity;
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
public class CopySecurityLootFunction extends LootItemConditionalFunction {

    public static final Codec<CopySecurityLootFunction> CODEC = RecordCodecBuilder.create(instance -> commonFields(instance)
          .apply(instance, CopySecurityLootFunction::new)
    );

    private CopySecurityLootFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    @Override
    public LootItemFunctionType getType() {
        return MekanismLootFunctions.COPY_SECURITY.get();
    }

    @Override
    public ItemStack run(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof ISecurityTile tile) {
            TileComponentSecurity security = tile.getSecurity();
            SecurityObject securityObject = stack.getData(MekanismAttachmentTypes.SECURITY);
            securityObject.setSecurityMode(security.getMode());
            securityObject.setOwnerUUID(security.getOwnerUUID());
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

    public static class Builder extends LootItemConditionalFunction.Builder<CopySecurityLootFunction.Builder> {

        protected Builder() {
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopySecurityLootFunction(getConditions());
        }
    }
}