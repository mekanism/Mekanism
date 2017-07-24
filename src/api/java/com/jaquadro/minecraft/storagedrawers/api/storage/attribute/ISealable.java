package com.jaquadro.minecraft.storagedrawers.api.storage.attribute;

public interface ISealable
{
    /**
     * Gets whether or not the drawer has the sealed attribute.
     * A sealed drawer cannot be interacted with, and when broken will retain all of its items and upgrades.
     */
    boolean isSealed ();

    /**
     * Sets whether or not the drawer is currently sealed.
     * @return false if the operation is not supported, true otherwise.
     */
    boolean setIsSealed (boolean state);
}
