package mekanism.api.transmitters;


import net.minecraft.util.Direction;

public interface IBlockableConnection {

    boolean canConnectMutual(Direction side);

    boolean canConnect(Direction side);
}