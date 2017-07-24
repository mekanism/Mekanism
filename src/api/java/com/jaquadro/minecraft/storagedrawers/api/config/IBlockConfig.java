package com.jaquadro.minecraft.storagedrawers.api.config;

import com.jaquadro.minecraft.storagedrawers.api.pack.BlockConfiguration;

public interface IBlockConfig
{
    String getBlockConfigName (BlockConfiguration blockConfig);

    boolean isBlockEnabled (String blockConfigName);

    int getBlockRecipeOutput (String blockConfigName);

    int getBaseCapacity (String blockConfigName);
}
