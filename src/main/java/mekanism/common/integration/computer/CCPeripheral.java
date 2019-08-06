package mekanism.common.integration.computer;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import javax.annotation.Nonnull;
import mekanism.common.integration.MekanismHooks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

/**
 * Created by aidancbrady on 7/20/15.
 */
@Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = MekanismHooks.COMPUTERCRAFT_MOD_ID)
public class CCPeripheral implements IPeripheral {

    public IComputerIntegration computerTile;

    public CCPeripheral(IComputerIntegration tile) {
        computerTile = tile;
    }

    @Nonnull
    @Override
    @Optional.Method(modid = MekanismHooks.COMPUTERCRAFT_MOD_ID)
    public String getType() {
        return computerTile.getName();
    }

    @Nonnull
    @Override
    @Optional.Method(modid = MekanismHooks.COMPUTERCRAFT_MOD_ID)
    public String[] getMethodNames() {
        return computerTile.getMethods();
    }

    @Override
    @Optional.Method(modid = MekanismHooks.COMPUTERCRAFT_MOD_ID)
    public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments) {
        try {
            return computerTile.invoke(method, arguments);
        } catch (NoSuchMethodException e) {
            return new Object[]{"Unknown command."};
        }
    }

    @Override
    @Optional.Method(modid = MekanismHooks.COMPUTERCRAFT_MOD_ID)
    public void attach(@Nonnull IComputerAccess computer) {
    }

    @Override
    @Optional.Method(modid = MekanismHooks.COMPUTERCRAFT_MOD_ID)
    public void detach(@Nonnull IComputerAccess computer) {
    }

    @Override
    @Optional.Method(modid = MekanismHooks.COMPUTERCRAFT_MOD_ID)
    public boolean equals(IPeripheral other) {
        return this == other;
    }

    public static class CCPeripheralProvider implements IPeripheralProvider {

        @Override
        public IPeripheral getPeripheral(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Direction side) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IComputerIntegration) {
                return new CCPeripheral((IComputerIntegration) te);
            }
            return null;
        }
    }
}