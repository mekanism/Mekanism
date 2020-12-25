package mekanism.common.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.content.qio.IQIOCraftingWindowHolder;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class PortableQIODashboardInventory extends ItemStackMekanismInventory implements IQIOCraftingWindowHolder {

    @Nullable
    private final World world;
    private final UUID playerUUID;
    /**
     * @apiNote This is only not final for purposes of being able to assign and use it in getInitialInventory.
     */
    private QIOCraftingWindow[] craftingWindows;

    public PortableQIODashboardInventory(ItemStack stack, @Nullable PlayerInventory inv) {
        super(stack);
        if (inv == null) {
            this.world = null;
            this.playerUUID = null;
        } else {
            this.world = inv.player.getEntityWorld();
            this.playerUUID = inv.player.getUniqueID();
        }
    }

    @Override
    protected List<IInventorySlot> getInitialInventory() {
        List<IInventorySlot> slots = new ArrayList<>();
        craftingWindows = new QIOCraftingWindow[MAX_CRAFTING_WINDOWS];
        for (byte tableIndex = 0; tableIndex < craftingWindows.length; tableIndex++) {
            QIOCraftingWindow craftingWindow =  new QIOCraftingWindow(this, tableIndex);
            craftingWindows[tableIndex] = craftingWindow;
            for (int slot = 0; slot < 9; slot++) {
                slots.add(craftingWindow.getInputSlot(slot));
            }
            slots.add(craftingWindow.getOutputSlot());
        }
        return slots;
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
        if (world != null && !world.isRemote()) {
            FrequencyIdentity identity = ((IFrequencyItem) stack.getItem()).getFrequency(stack);
            if (identity == null) {
                return null;
            }
            FrequencyManager<QIOFrequency> manager = identity.isPublic() ? FrequencyType.QIO.getManager(null) : FrequencyType.QIO.getManager(playerUUID);
            QIOFrequency freq = manager.getFrequency(identity.getKey());
            // if this frequency no longer exists, remove the reference from the stack
            if (freq == null) {
                ((IFrequencyItem) stack.getItem()).setFrequency(stack, null);
            }
            return freq;
        }
        return null;
    }
}