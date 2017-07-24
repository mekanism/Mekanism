package com.jaquadro.minecraft.storagedrawers.api.storage.attribute;

public interface IShroudable
{
    /**
     * Gets whether or not the drawer has the shrouded attribute.
     * The shrouded attribute instructs the drawer to not render its item label.
     */
    boolean isShrouded ();

    /**
     * Sets whether or not the drawer is currently shrouded.
     * @return false if the operation is not supported, true otherwise.
     */
    boolean setIsShrouded (boolean state);
}
