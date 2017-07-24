package com.jaquadro.minecraft.storagedrawers.api.storage.attribute;

public interface IQuantifiable
{
    /**
     * Gets whether or not the drawer has the quantified attribute.
     * The quantified attribute instructs the drawer to render its numerical quantity.
     */
    boolean isShowingQuantity ();

    /**
     * Sets whether or not the drawer is currently quantified.
     * @return false if the operation is not supported, true otherwise.
     */
    boolean setIsShowingQuantity (boolean state);
}
