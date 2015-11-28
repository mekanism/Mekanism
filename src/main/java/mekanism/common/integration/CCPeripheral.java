package mekanism.common.integration;

import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by aidancbrady on 7/20/15.
 */
@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")
public class CCPeripheral implements IPeripheral
{
    public IComputerIntegration computerTile;

    public CCPeripheral(IComputerIntegration tile)
    {
        computerTile = tile;
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String getType()
    {
        return computerTile.getInventoryName();
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String[] getMethodNames()
    {
        return computerTile.getMethods();
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
    {
        try {
            return computerTile.invoke(method, arguments);
        } catch(NoSuchMethodException e) {
            return new Object[] {"Unknown command."};
        } catch(Exception e) {
        	e.printStackTrace();
        	return new Object[] {"Error."};
        }
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public void attach(IComputerAccess computer) {}

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public void detach(IComputerAccess computer) {}

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public boolean equals(IPeripheral other)
    {
        return this == other;
    }

    public static class CCPeripheralProvider implements IPeripheralProvider
    {
        @Override
        public IPeripheral getPeripheral(World world, int x, int y, int z, int side)
        {
            TileEntity te = world.getTileEntity(x, y, z);

            if(te != null && te instanceof IComputerIntegration)
            {
                return new CCPeripheral((IComputerIntegration)te);
            }

            return null;
        }
    }
}
