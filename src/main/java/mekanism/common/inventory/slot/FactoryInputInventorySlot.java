package mekanism.common.inventory.slot;

import java.util.Objects;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.tile.factory.TileEntityFactory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FactoryInputInventorySlot extends InputInventorySlot {

    public static FactoryInputInventorySlot create(TileEntityFactory<?> factory, int process, IInventorySlot outputSlot, @Nullable IContentsListener listener,
          int x, int y) {
        return create(factory, process, outputSlot, null, listener, x, y);
    }

    public static FactoryInputInventorySlot create(TileEntityFactory<?> factory, int process, IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot,
          @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(factory, "Factory cannot be null");
        Objects.requireNonNull(outputSlot, "Primary output slot cannot be null");
        return new FactoryInputInventorySlot(factory, process, outputSlot, secondaryOutputSlot, listener, x, y);
    }

    private FactoryInputInventorySlot(TileEntityFactory<?> factory, int process, IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot,
          @Nullable IContentsListener listener, int x, int y) {
        super(stack -> factory.inputProducesOutput(process, stack, outputSlot, secondaryOutputSlot, false), factory::isValidInputItem, listener, x, y);
    }

    //Increase access level of setStackUnchecked
    @Override
    public void setStackUnchecked(ItemStack stack) {
        super.setStackUnchecked(stack);
    }
}