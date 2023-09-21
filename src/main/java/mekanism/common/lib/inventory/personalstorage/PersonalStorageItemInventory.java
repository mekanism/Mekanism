package mekanism.common.lib.inventory.personalstorage;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.DataHandlerUtils;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

/**
 * Inventory for Personal Storages when an item. Handled by the Block when placed in world.
 */
@NothingNullByDefault
public class PersonalStorageItemInventory extends AbstractPersonalStorageItemInventory implements INBTSerializable<ListTag> {

    private final IContentsListener parent;

    PersonalStorageItemInventory(IContentsListener parent) {
        this.parent = parent;
    }

    @Override
    public void onContentsChanged() {
        parent.onContentsChanged();
    }

    @Override
    public ListTag serializeNBT() {
        return DataHandlerUtils.writeContainers(this.slots);
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        DataHandlerUtils.readContainers(this.slots, nbt);
    }
}
