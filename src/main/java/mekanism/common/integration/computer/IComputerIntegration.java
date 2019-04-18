package mekanism.common.integration.computer;

import net.minecraft.inventory.IInventory;

/**
 * Created by aidancbrady on 7/20/15.
 */
public interface IComputerIntegration extends IInventory {

    String[] getMethods();

    Object[] invoke(int method, Object[] args) throws NoSuchMethodException;
}
