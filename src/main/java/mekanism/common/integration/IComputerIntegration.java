package mekanism.common.integration;

import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.inventory.IInventory;

/**
 * Created by aidancbrady on 7/20/15.
 */
@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
public interface IComputerIntegration extends IInventory, IPeripheral
{
    public String[] getMethods();

    public Object[] invoke(int method, Object[] args) throws Exception;
}
