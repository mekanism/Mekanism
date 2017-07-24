package com.jaquadro.minecraft.storagedrawers.api.storage;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface ISmartGroup
{
    /**
     * Gets a lazy enumeration of all slots that will accept at least one item of the given stack, ordered by
     * insertion preference.
     */
    Iterable<Integer> enumerateDrawersForInsertion (@Nonnull ItemStack stack, boolean strict);

    /**
     * Gets a lazy enumeration of all slots that will provide at least one item of the given stack, ordered by
     * extraction preference.
     */
    Iterable<Integer> enumerateDrawersForExtraction (@Nonnull ItemStack stack, boolean strict);
}
