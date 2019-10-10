package mekanism.common.tile.factory;

import mekanism.api.annotations.NonNull;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import net.minecraft.item.ItemStack;

public abstract class TileEntityItemToItemFactory<RECIPE extends MekanismRecipe> extends TileEntityFactory<RECIPE> {

    protected final IInputHandler<@NonNull ItemStack>[] inputHandlers;
    protected final IOutputHandler<@NonNull ItemStack>[] outputHandlers;

    protected TileEntityItemToItemFactory(IBlockProvider blockProvider) {
        super(blockProvider);

        inputHandlers = new IInputHandler[tier.processes];
        outputHandlers = new IOutputHandler[tier.processes];
        for (int i = 0; i < tier.processes; i++) {
            inputHandlers[i] = InputHelper.getInputHandler(this, getInputSlot(i));
            outputHandlers[i] = OutputHelper.getOutputHandler(this, getOutputSlot(i));
        }
    }
}