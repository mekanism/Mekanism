package mekanism.common.item.block.machine;

import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.chemical.variable.RateLimitGasTank;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.lookup.cache.InputRecipeCache.ItemChemical;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import net.neoforged.bus.api.IEventBus;

public abstract class ItemBlockAdvancedElectricMachine extends ItemBlockMachine {

    public ItemBlockAdvancedElectricMachine(BlockTile<?, ?> block) {
        super(block);
    }

    protected abstract IMekanismRecipeTypeProvider<ItemStackGasToItemStackRecipe, ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> getRecipeType();

    @Override
    public void attachAttachments(IEventBus eventBus) {
        super.attachAttachments(eventBus);
        //Note: We pass null for the event bus to not expose this attachment as a capability
        ContainerType.GAS.addDefaultContainer(null, this, stack -> RateLimitGasTank.createBasicItem(TileEntityAdvancedElectricMachine.MAX_GAS,
              ChemicalTankBuilder.GAS.manualOnly, ChemicalTankBuilder.GAS.alwaysTrueBi,
              gas -> getRecipeType().getInputCache().containsInputB(null, gas.getStack(1))
        ));
    }
}