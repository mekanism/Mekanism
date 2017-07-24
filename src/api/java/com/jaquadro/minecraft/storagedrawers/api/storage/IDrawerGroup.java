package com.jaquadro.minecraft.storagedrawers.api.storage;

public interface IDrawerGroup
{
    /**
     * Gets the number of drawers contained within this group.
     */
    int getDrawerCount ();

    /**
     * Gets the drawer at the given slot within this group.
     */
    IDrawer getDrawer (int slot);

    /**
     * Gets the drawer at the given slot within this group only if it is enabled.
     */
    IDrawer getDrawerIfEnabled (int slot);

    /**
     * Gets whether the drawer in the given slot is usable.
     */
    boolean isDrawerEnabled (int slot);

    boolean markDirtyIfNeeded ();
}
