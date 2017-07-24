package com.jaquadro.minecraft.storagedrawers.api.storage;

/**
 * Represents a drawer with items that are a fractional component of another item within the drawer group.  Compacting
 * Drawers are a primary example of this drawer type.
 */
public interface IFractionalDrawer extends IDrawer
{
    /**
     * Gets the storage ratio between the held item and the most compressed item within the drawer group.
     *
     * For example, most ingots have a conversion rate of 9 compared to metal blocks, and nuggets a rate of 81.
     * Actual conversion rates are implementation-defined.
     */
    int getConversionRate ();

    /**
     * Gets the number of items left in the drawer if the maximum number of equivalent compressed items had been removed.
     * The equivalency is determined by the next compression tier, and not necessarily the conversion rate.
     */
    int getStoredItemRemainder ();

    /**
     * Gets whether or not the stored item represents the smallest granularity of material that can be stored within
     * the drawer group.
     */
    boolean isSmallestUnit ();
}
