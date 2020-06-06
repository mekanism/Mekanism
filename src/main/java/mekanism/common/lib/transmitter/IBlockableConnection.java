package mekanism.common.lib.transmitter;


import javax.annotation.Nullable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

@Deprecated//TODO - V10: Remove/Rework?
public interface IBlockableConnection {

    boolean canConnectMutual(Direction side, @Nullable TileEntity cachedTile);

    boolean canConnect(Direction side);
}