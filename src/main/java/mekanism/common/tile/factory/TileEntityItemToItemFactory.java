package mekanism.common.tile.factory;

import mekanism.api.annotations.NonNull;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.base.ProcessInfo;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.FactoryInputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tier.FactoryTier;
import net.minecraft.item.ItemStack;

public abstract class TileEntityItemToItemFactory<RECIPE extends MekanismRecipe> extends TileEntityFactory<RECIPE> {

    protected IInputHandler<@NonNull ItemStack>[] inputHandlers;
    protected IOutputHandler<@NonNull ItemStack>[] outputHandlers;

    protected TileEntityItemToItemFactory(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void addSlots(InventorySlotHelper builder) {
        inputHandlers = new IInputHandler[tier.processes];
        outputHandlers = new IOutputHandler[tier.processes];
        processInfoSlots = new ProcessInfo[tier.processes];
        int baseX = tier == FactoryTier.BASIC ? 55 : tier == FactoryTier.ADVANCED ? 35 : tier == FactoryTier.ELITE ? 29 : 27;
        int baseXMult = tier == FactoryTier.BASIC ? 38 : tier == FactoryTier.ADVANCED ? 26 : 19;
        for (int i = 0; i < tier.processes; i++) {
            int xPos = baseX + (i * baseXMult);
            OutputInventorySlot outputSlot = OutputInventorySlot.at(this, xPos, 57);
            IInventorySlot inputSlot = FactoryInputInventorySlot.create(this, i, outputSlot, this, xPos, 13);
            builder.addSlot(inputSlot);
            builder.addSlot(outputSlot);
            inputHandlers[i] = InputHelper.getInputHandler(inputSlot);
            outputHandlers[i] = OutputHelper.getOutputHandler(outputSlot);
            processInfoSlots[i] = new ProcessInfo(i, inputSlot, outputSlot, null);
        }
    }
}