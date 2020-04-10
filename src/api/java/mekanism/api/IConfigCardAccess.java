package mekanism.api;

import net.minecraft.nbt.CompoundNBT;

/**
 * Implement this in your TileEntity class if you wish for Mekanism filters to be able to store any of their information.
 *
 * @author aidancbrady
 */
public interface IConfigCardAccess {

    interface ISpecialConfigData extends IConfigCardAccess {

        /**
         * Collects the TileEntity's filter card data into the parameterized CompoundNBT.
         *
         * @param nbtTags - the CompoundNBT of the filter card ItemStack
         *
         * @return the CompoundNBT that now contains the TileEntity's filter card data
         */
        CompoundNBT getConfigurationData(CompoundNBT nbtTags);

        /**
         * Retrieves the TileEntity's data contained in the filter card based on the given CompoundNBT.
         *
         * @param nbtTags - the CompoundNBT of the filter card ItemStack
         */
        void setConfigurationData(CompoundNBT nbtTags);

        /**
         * A String name of this TileEntity that will be displayed as the type of data on the filter card.
         *
         * @return the String name of this TileEntity
         */
        String getDataType();
    }
}