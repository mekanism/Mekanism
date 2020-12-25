package mekanism.common.tile.qio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.world.World;

public class TileEntityQIODashboard extends TileEntityQIOComponent implements IQIOCraftingWindowHolder {

    /**
     * @apiNote This is only not final for purposes of being able to assign it in presetVariables so that we can use it in getInitialInventory.
     */
    private QIOCraftingWindow[] craftingWindows;

    public TileEntityQIODashboard() {
        super(MekanismBlocks.QIO_DASHBOARD);
    }

    @Override
    protected void presetVariables() {
        super.presetVariables();
        craftingWindows = new QIOCraftingWindow[MAX_CRAFTING_WINDOWS];
        for (byte tableIndex = 0; tableIndex < craftingWindows.length; tableIndex++) {
            craftingWindows[tableIndex] = new QIOCraftingWindow(this, tableIndex);
        }
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        //TODO - 10.1: Re-evaluate/make an improved performance ItemHandlerManager that uses this method
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
        if (world.getGameTime() % 10 == 0) {
            setActive(getQIOFrequency() != null);
        }
    }

    @Nullable
    @Override
    public World getHolderWorld() {
        return world;
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
}