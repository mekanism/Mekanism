package com.jaquadro.minecraft.storagedrawers.api.storage.attribute;

import com.jaquadro.minecraft.storagedrawers.api.security.ISecurityProvider;
import net.minecraft.world.ILockableContainer;

import java.util.UUID;

public interface IProtectable
{
    /**
     * Gets the owner of the drawer, or null if there is no owner.
     */
    UUID getOwner ();

    /**
     * Sets the owner of the drawer.  Set to null to set no owner.
     * @return false if the operation is not supported, true otherwise.
     */
    boolean setOwner (UUID owner);

    /**
     * Gets the provider managing security for the target.
     * @return null to use the default provider, which enforces strict owner access.
     */
    ISecurityProvider getSecurityProvider ();

    /**
     * Gets the lockable interface if it exists on the protected drawer.
     * @return A lockable interface, or null if lockable is not supported.
     */
    ILockableContainer getLockableContainer ();

    /**
     * Sets the provider managing security for the target.  Set to null for default provider.
     * @return false if the operation is not supported, true otherwise.
     */
    boolean setSecurityProvider (ISecurityProvider provder);
}
