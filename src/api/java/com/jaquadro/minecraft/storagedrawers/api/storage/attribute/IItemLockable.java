package com.jaquadro.minecraft.storagedrawers.api.storage.attribute;

public interface IItemLockable
{
    /**
     * Gets whether or not a drawer or group is locked for the given lock attribute.
     */
    boolean isItemLocked (LockAttribute attr);

    /**
     * Gets whether or not the lock state can be changed for the given lock attribute.
     * If this method returns false, isItemLocked may still return true.
     */
    boolean canItemLock (LockAttribute attr);

    /**
     * Sets the lock state of a drawer or group for the given lock attribute.
     * If canItemLock returns false, this is a no-op.
     */
    void setItemLocked (LockAttribute attr, boolean isLocked);
}
