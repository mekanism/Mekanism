package mekanism.api.transmitters;


import net.minecraft.util.EnumFacing;

public interface IBlockableConnection {

    boolean canConnectMutual(EnumFacing side);

    boolean canConnect(EnumFacing side);
}
