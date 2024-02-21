package mekanism.common.item.loot;

import java.util.Set;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.tile.interfaces.IUpgradeTile;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Loot function which copies upgrade data to the stack's attachments
 */
@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class CopyUpgradesLootFunction implements LootItemFunction {

    public static final CopyUpgradesLootFunction INSTANCE = new CopyUpgradesLootFunction();

    private CopyUpgradesLootFunction() {
    }

    @Override
    public LootItemFunctionType getType() {
        return MekanismLootFunctions.COPY_UPGRADES.get();
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof IUpgradeTile tile) {
            stack.getData(MekanismAttachmentTypes.UPGRADES).copyFrom(tile.getComponent());
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