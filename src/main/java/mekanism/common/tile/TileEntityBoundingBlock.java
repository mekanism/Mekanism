package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileNetwork;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Multi-block used by wind turbines, solar panels, and other machines
 */
public class TileEntityBoundingBlock extends TileEntity implements ITileNetwork {

    private BlockPos mainPos = BlockPos.ZERO;

    public boolean receivedCoords;

    public int prevPower;

    public TileEntityBoundingBlock() {
        this(MekanismTileEntityTypes.BOUNDING_BLOCK);
    }

    public TileEntityBoundingBlock(TileEntityType<TileEntityBoundingBlock> type) {
        super(type);
    }

    public void setMainLocation(BlockPos pos) {
        receivedCoords = pos != null;
        if (!world.isRemote) {
            mainPos = pos;
            Mekanism.packetHandler.sendUpdatePacket(this);
        }
    }

    public BlockPos getMainPos() {
        if (mainPos == null) {
            mainPos = BlockPos.ZERO;
        }
        return mainPos;
    }

    @Override
    public void validate() {
        super.validate();
        if (world.isRemote) {
            Mekanism.packetHandler.sendToServer(new PacketDataRequest(Coord4D.get(this)));
        }
    }

    public TileEntity getMainTile() {
        if (receivedCoords && world.isAreaLoaded(getMainPos(), 0)) {
            return world.getTileEntity(getMainPos());
        }
        return null;
    }

    public void onNeighborChange(Block block) {
        final TileEntity tile = getMainTile();
        if (tile instanceof TileEntityMekanism) {
            TileEntityMekanism tileEntity = (TileEntityMekanism) tile;
            int power = world.getRedstonePowerFromNeighbors(getPos());
            if (prevPower != power) {
                if (power > 0) {
                    onPower();
                } else {
                    onNoPower();
                }
                prevPower = power;
                Mekanism.packetHandler.sendToAllTracking(new PacketTileEntity(tileEntity), this);
            }
        }
    }

    public void onPower() {
    }

    public void onNoPower() {
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (world.isRemote) {
            mainPos = new BlockPos(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
            prevPower = dataStream.readInt();
            receivedCoords = dataStream.readBoolean();
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        mainPos = new BlockPos(nbtTags.getInt("mainX"), nbtTags.getInt("mainY"), nbtTags.getInt("mainZ"));
        prevPower = nbtTags.getInt("prevPower");
        receivedCoords = nbtTags.getBoolean("receivedCoords");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("mainX", getMainPos().getX());
        nbtTags.putInt("mainY", getMainPos().getY());
        nbtTags.putInt("mainZ", getMainPos().getZ());
        nbtTags.putInt("prevPower", prevPower);
        nbtTags.putBoolean("receivedCoords", receivedCoords);
        return nbtTags;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        data.add(getMainPos().getX());
        data.add(getMainPos().getY());
        data.add(getMainPos().getZ());
        data.add(prevPower);
        data.add(receivedCoords);
        return data;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.TILE_NETWORK_CAPABILITY) {
            return Capabilities.TILE_NETWORK_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }
}