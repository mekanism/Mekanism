package mekanism.common.lib.inventory.personalstorage;

/**
 * Dummy inventory for client side to sync to
 */
public class ClientSidePersonalStorageInventory extends AbstractPersonalStorageItemInventory {

    @Override
    public void onContentsChanged() {
        //no op
    }
}
