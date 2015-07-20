package mekanism.common.integration;

import net.minecraft.inventory.IInventory;

/**
 * Created by aidancbrady on 7/20/15.
 */
public interface IComputerIntegration extends IInventory
{
    public String[] getMethods();

    public Object[] invoke(int method, Object[] args) throws Exception;
}
