package mekanism.common.inventory.slot;

import javax.annotation.Nullable;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.tile.factory.TileEntityFactory;

public class FactoryInputInventorySlot<RECIPE extends MekanismRecipe> extends InputInventorySlot {

    public static <RECIPE extends MekanismRecipe> FactoryInputInventorySlot create(TileEntityFactory<RECIPE> factory, int process, IInventorySlot outputSlot, int x, int y) {
        return create(factory, process, outputSlot, null, x, y);
    }

    public static <RECIPE extends MekanismRecipe> FactoryInputInventorySlot create(TileEntityFactory<RECIPE> factory, int process, IInventorySlot outputSlot,
          @Nullable IInventorySlot secondaryOutputSlot, int x, int y) {
        return new FactoryInputInventorySlot<>(factory, process, outputSlot, secondaryOutputSlot, x, y);
    }

    private FactoryInputInventorySlot(TileEntityFactory<RECIPE> factory, int process, IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot, int x, int y) {
        super(item -> factory.inputProducesOutput(process, item, outputSlot, secondaryOutputSlot, false), factory::isValidInputItem, x, y);
    }
}