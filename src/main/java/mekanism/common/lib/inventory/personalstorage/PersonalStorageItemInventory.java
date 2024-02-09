package mekanism.common.lib.inventory.personalstorage;

import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;

/**
 * Inventory for Personal Storages when an item. Handled by the Block when placed in world.
 */
@NothingNullByDefault
public class PersonalStorageItemInventory extends AbstractPersonalStorageItemInventory {

    private final IContentsListener parent;

    PersonalStorageItemInventory(IContentsListener parent) {
        this.parent = parent;
    }

    @Override
    public void onContentsChanged() {
        parent.onContentsChanged();
    }
}
