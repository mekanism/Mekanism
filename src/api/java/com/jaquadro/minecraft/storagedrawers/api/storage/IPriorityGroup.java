package com.jaquadro.minecraft.storagedrawers.api.storage;


public interface IPriorityGroup
{
    /**
     * Gets the list of available drawer slots in priority order.
     */
    int[] getAccessibleDrawerSlots ();
}
