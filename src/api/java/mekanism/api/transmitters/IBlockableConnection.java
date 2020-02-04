package mekanism.api.transmitters;


import javax.annotation.Nullable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public interface IBlockableConnection {

    boolean canConnectMutual(Direction side, @Nullable TileEntity cachedTile);

    boolean canConnect(Direction side);
}