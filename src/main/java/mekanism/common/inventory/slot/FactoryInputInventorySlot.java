package mekanism.common.inventory.slot;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.item.ItemStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FactoryInputInventorySlot<RECIPE extends MekanismRecipe> extends InputInventorySlot {

    public static <RECIPE extends MekanismRecipe> FactoryInputInventorySlot<RECIPE> create(TileEntityFactory<RECIPE> factory, int process, IInventorySlot outputSlot,
          @Nullable IContentsListener listener, int x, int y) {
        return create(factory, process, outputSlot, null, listener, x, y);
    }

    public static <RECIPE extends MekanismRecipe> FactoryInputInventorySlot<RECIPE> create(TileEntityFactory<RECIPE> factory, int process, IInventorySlot outputSlot,
          @Nullable IInventorySlot secondaryOutputSlot, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(factory, "Factory cannot be null");
        Objects.requireNonNull(outputSlot, "Primary output slot cannot be null");
        return new FactoryInputInventorySlot<>(factory, process, outputSlot, secondaryOutputSlot, listener, x, y);
    }

    private FactoryInputInventorySlot(TileEntityFactory<RECIPE> factory, int process, IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot,
          @Nullable IContentsListener listener, int x, int y) {
        super(stack -> factory.inputProducesOutput(process, stack, outputSlot, secondaryOutputSlot, false), factory::isValidInputItem, listener, x, y);
    }

    //Increase access level of setStackUnchecked
    @Override
    public void setStackUnchecked(ItemStack stack) {
        super.setStackUnchecked(stack);
    }
}