package mekanism.common.inventory.slot;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.tile.factory.TileEntityFactory;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FactoryInputInventorySlot<RECIPE extends MekanismRecipe> extends InputInventorySlot {

    public static <RECIPE extends MekanismRecipe> FactoryInputInventorySlot<RECIPE> create(TileEntityFactory<RECIPE> factory, int process, IInventorySlot outputSlot,
          @Nullable IMekanismInventory inventory, int x, int y) {
        return create(factory, process, outputSlot, null, inventory, x, y);
    }

    public static <RECIPE extends MekanismRecipe> FactoryInputInventorySlot<RECIPE> create(TileEntityFactory<RECIPE> factory, int process, IInventorySlot outputSlot,
          @Nullable IInventorySlot secondaryOutputSlot, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(factory, "Factory cannot be null");
        Objects.requireNonNull(outputSlot, "Primary output slot cannot be null");
        return new FactoryInputInventorySlot<>(factory, process, outputSlot, secondaryOutputSlot, inventory, x, y);
    }

    private FactoryInputInventorySlot(TileEntityFactory<RECIPE> factory, int process, IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot,
          @Nullable IMekanismInventory inventory, int x, int y) {
        super(stack -> factory.inputProducesOutput(process, stack, outputSlot, secondaryOutputSlot, false), factory::isValidInputItem, inventory, x, y);
    }
}