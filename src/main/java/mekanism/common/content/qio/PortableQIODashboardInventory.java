package mekanism.common.content.qio;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.FrequencyAware;
import mekanism.common.attachments.qio.PortableDashboardContents;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

public class PortableQIODashboardInventory implements IQIOCraftingWindowHolder {

    private final QIOCraftingWindow[] craftingWindows;
    private final List<IInventorySlot> slots;
    private final ItemAccess itemAccess;
    @Nullable
    private final Level level;
    private boolean initializing;

    public PortableQIODashboardInventory(@Nullable Level level, ItemAccess itemAccess) {
        this.itemAccess = itemAccess;
        this.level = level;
        List<IInventorySlot> slots = new ArrayList<>();
        craftingWindows = new QIOCraftingWindow[MAX_CRAFTING_WINDOWS];
        List<LargeResourceStack<ItemResource>> contents = itemAccess.getResource().getOrDefault(MekanismDataComponents.QIO_DASHBOARD, PortableDashboardContents.EMPTY).contents();
        initializing = true;
        for (int tableIndex = 0; tableIndex < craftingWindows.length; tableIndex++) {
            int finalTableIndex = tableIndex;
            QIOCraftingWindow craftingWindow = new QIOCraftingWindow(this, (byte) tableIndex, slot -> () -> {
                //Skip contents change handling until we actually have our crafting window updated
                if (!initializing) {
                    IInventorySlot inputSlot = craftingWindows[finalTableIndex].getInputSlot(slot);
                    ItemResource resource = this.itemAccess.getResource();
                    PortableDashboardContents content = resource.getOrDefault(MekanismDataComponents.QIO_DASHBOARD, PortableDashboardContents.EMPTY);
                    resource = resource.with(MekanismDataComponents.QIO_DASHBOARD, content.with(finalTableIndex, slot, inputSlot.asStack()));
                    //Note: This save listener is called from within `SnapshotJournal#onRootCommit`, but it is safe to open a new transaction
                    // from here thanks to https://github.com/neoforged/NeoForge/pull/2714
                    ItemAccessUtils.exchange(this.itemAccess, resource, null);
                }
            });
            craftingWindows[tableIndex] = craftingWindow;
            for (int slot = 0; slot < QIOCraftingWindow.SLOTS_PER_WINDOW; slot++) {
                IInventorySlot inputSlot = craftingWindow.getInputSlot(slot);
                slots.add(inputSlot);
                inputSlot.setContents(contents.get(tableIndex * QIOCraftingWindow.SLOTS_PER_WINDOW + slot), null);
            }
            slots.add(craftingWindow.getOutputSlot());
        }
        this.slots = List.copyOf(slots);
        initializing = false;
    }

    public List<IInventorySlot> getSlots() {
        return slots;
    }

    @Nullable
    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public QIOCraftingWindow[] getCraftingWindows() {
        return craftingWindows;
    }

    @Nullable
    @Override
    public QIOFrequency getFrequency() {
        if (level != null && !level.isClientSide()) {
            ItemResource resource = itemAccess.getResource();
            if (!resource.isEmpty()) {
                FrequencyAware<QIOFrequency> frequencyAware = resource.get(MekanismDataComponents.QIO_FREQUENCY);
                if (frequencyAware != null) {
                    return frequencyAware.frequency().orElse(null);
                }
            }
        }
        return null;
    }

    @Override
    public void onContentsChanged() {
    }
}