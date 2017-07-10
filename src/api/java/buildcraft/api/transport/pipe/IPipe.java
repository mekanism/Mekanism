package buildcraft.api.transport.pipe;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IPipe extends ICapabilityProvider {
    IPipeHolder getHolder();

    PipeDefinition getDefinition();

    PipeBehaviour getBehaviour();

    PipeFlow getFlow();

    EnumDyeColor getColour();

    void setColour(EnumDyeColor colour);

    void markForUpdate();

    TileEntity getConnectedTile(EnumFacing side);

    IPipe getConnectedPipe(EnumFacing side);

    boolean isConnected(EnumFacing side);

    ConnectedType getConnectedType(EnumFacing side);

    enum ConnectedType {
        TILE,
        PIPE
    }
}
