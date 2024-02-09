package mekanism.common.item.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.radiation.IRadiationManager;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.base.TileEntityMekanism;
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
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

/**
 * Loot function which copies containers to the stack's attachments
 */
@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class CopyContainersLootFunction extends LootItemConditionalFunction {

    public static final Codec<CopyContainersLootFunction> CODEC = RecordCodecBuilder.create(instance -> commonFields(instance)
          .and(NeoForgeExtraCodecs.withAlternative(
                ContainerType.CODEC.<List<ContainerType<?, ?, ?>>>flatComapMap(List::of, list -> {
                    if (list.size() == 1) {
                        return DataResult.success(list.get(0));
                    }
                    return DataResult.error(() -> "Must be a single container type to be represented as a direct reference");
                }).fieldOf("type"),
                ContainerType.CODEC.listOf().fieldOf("types")
          ).forGetter(function -> function.containerTypes))
          .apply(instance, CopyContainersLootFunction::new)
    );

    private final List<ContainerType<?, ?, ?>> containerTypes;

    private CopyContainersLootFunction(List<LootItemCondition> conditions, List<ContainerType<?, ?, ?>> containerTypes) {
        super(conditions);
        this.containerTypes = containerTypes;
    }

    @Override
    public LootItemFunctionType getType() {
        return MekanismLootFunctions.COPY_CONTAINERS.get();
    }

    @Override
    public ItemStack run(ItemStack stack, LootContext lootContext) {
        BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof TileEntityMekanism tile) {
            for (ContainerType<?, ?, ?> containerType : this.containerTypes) {
                containerType.copyTo(tile, stack);
            }
            //Skip tiles that have no gas tanks and skip the creative chemical tank
            if (IRadiationManager.INSTANCE.isRadiationEnabled() && !tile.getGasTanks(null).isEmpty()) {
                if (tile instanceof TileEntityChemicalTank chemicalTank && chemicalTank.getTier() == ChemicalTankTier.CREATIVE) {
                    return stack;
                }
                for (IGasTank tank : ContainerType.GAS.getAttachmentContainersIfPresent(stack)) {
                    if (!tank.isEmpty() && tank.getStack().has(GasAttributes.Radiation.class)) {
                        //If the tank isn't empty and has a radioactive gas in it, clear the tank
                        tank.setEmpty();
                    }
                }
            }
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

    public static class Builder extends LootItemConditionalFunction.Builder<CopyContainersLootFunction.Builder> {

        private final List<ContainerType<?, ?, ?>> containerTypes = new ArrayList<>();

        protected Builder() {
        }

        public Builder copy(ContainerType<?, ?, ?> containerType) {
            containerTypes.add(containerType);
            return this;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new CopyContainersLootFunction(getConditions(), this.containerTypes);
        }
    }
}