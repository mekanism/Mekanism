package mekanism.common.tile.qio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IContentsListener;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityQIODashboard extends TileEntityQIOComponent implements IQIOCraftingWindowHolder {

    /**
     * @apiNote This is only not final for purposes of being able to assign it in presetVariables so that we can use it in getInitialInventory.
     */
    private QIOCraftingWindow[] craftingWindows;
    private boolean recipesChecked = false;

    public TileEntityQIODashboard(BlockPos pos, BlockState state) {
        super(MekanismBlocks.QIO_DASHBOARD, pos, state);
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        craftingWindows = new QIOCraftingWindow[MAX_CRAFTING_WINDOWS];
        for (byte tableIndex = 0; tableIndex < craftingWindows.length; tableIndex++) {
            //Note: We don't bother passing a special listener as:
            // a. We don't support comparators
            // b. If we did it would be of items which this would already be
            craftingWindows[tableIndex] = new QIOCraftingWindow(this, tableIndex);
        }
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        //TODO - 1.18: Re-evaluate/make an improved performance ItemHandlerManager that uses this method
        // that is for read only slots instead of actually exposing slots to various sides
        InventorySlotHelper builder = InventorySlotHelper.readOnly();
        for (QIOCraftingWindow craftingWindow : craftingWindows) {
            for (int slot = 0; slot < 9; slot++) {
                builder.addSlot(craftingWindow.getInputSlot(slot));
            }
            builder.addSlot(craftingWindow.getOutputSlot());
        }
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (CommonWorldTickHandler.flushTagAndRecipeCaches || !recipesChecked) {
            //If we need to update the recipes because of a reload or if we just haven't checked the recipes yet
            // after loading, as there was no world set yet, refresh the recipes
            recipesChecked = true;
            for (QIOCraftingWindow craftingWindow : craftingWindows) {
                craftingWindow.invalidateRecipe();
            }
        }
    }

    @Nullable
    @Override
    public Level getHolderWorld() {
        return level;
    }

    @Override
    public QIOCraftingWindow[] getCraftingWindows() {
        return craftingWindows;
    }

    @Nullable
    @Override
    public QIOFrequency getFrequency() {
        return getQIOFrequency();
    }

    //Methods relating to IComputerTile
    private void validateWindow(int window) throws ComputerException {
        if (window < 0 || window >= craftingWindows.length) {
            throw new ComputerException("Window '%d' is out of bounds, must be between 0 and %d.", window, craftingWindows.length);
        }
    }

    @ComputerMethod
    private ItemStack getCraftingInput(int window, int slot) throws ComputerException {
        validateWindow(window);
        if (slot < 0 || slot >= 9) {
            throw new ComputerException("Slot '%d' is out of bounds, must be between 0 and 9.", slot);
        }
        return craftingWindows[window].getInputSlot(slot).getStack();
    }

    @ComputerMethod
    private ItemStack getCraftingOutput(int window) throws ComputerException {
        validateWindow(window);
        return craftingWindows[window].getOutputSlot().getStack();
    }
    //End methods IComputerTile
}