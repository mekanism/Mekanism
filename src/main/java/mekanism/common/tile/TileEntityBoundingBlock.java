package mekanism.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.base.ITileNetwork;
import mekanism.api.TileNetworkList;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

public class TileEntityBoundingBlock extends TileEntity implements ITileNetwork {

    public BlockPos mainPos = BlockPos.ORIGIN;

    public boolean receivedCoords;

    public int prevPower;

    public void setMainLocation(BlockPos pos) {
        receivedCoords = true;

        if (!world.isRemote) {
            mainPos = pos;

            Mekanism.packetHandler
                  .sendToReceivers(new TileEntityMessage(Coord4D.get(this), getNetworkedData(new TileNetworkList())),
                        new Range4D(Coord4D.get(this)));
        }
    }

    @Override
    public void validate() {
        super.validate();

        if (world.isRemote) {
            Mekanism.packetHandler.sendToServer(new DataRequestMessage(Coord4D.get(this)));
        }
    }

    public void onNeighborChange(Block block) {
        TileEntity tile = world.getTileEntity(mainPos);

        if (tile instanceof TileEntityBasicBlock) {
            TileEntityBasicBlock tileEntity = (TileEntityBasicBlock) tile;

            int power = world.getRedstonePowerFromNeighbors(getPos());

            if (prevPower != power) {
                if (power > 0) {
                    onPower();
                } else {
                    onNoPower();
                }

                prevPower = power;
                Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tileEntity),
                      tileEntity.getNetworkedData(new TileNetworkList())), new Range4D(Coord4D.get(this)));
            }
        }
    }

    public void onPower() {
    }

    public void onNoPower() {
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        if (world.isRemote) {
            mainPos = new BlockPos(dataStream.readInt(), dataStream.readInt(), dataStream.readInt());
            prevPower = dataStream.readInt();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        mainPos = new BlockPos(nbtTags.getInteger("mainX"), nbtTags.getInteger("mainY"), nbtTags.getInteger("mainZ"));
        prevPower = nbtTags.getInteger("prevPower");
        receivedCoords = nbtTags.getBoolean("receivedCoords");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setInteger("mainX", mainPos.getX());
        nbtTags.setInteger("mainY", mainPos.getY());
        nbtTags.setInteger("mainZ", mainPos.getZ());
        nbtTags.setInteger("prevPower", prevPower);
        nbtTags.setBoolean("receivedCoords", receivedCoords);

        return nbtTags;
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        data.add(mainPos.getX());
        data.add(mainPos.getY());
        data.add(mainPos.getZ());
        data.add(prevPower);

        return data;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == Capabilities.TILE_NETWORK_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == Capabilities.TILE_NETWORK_CAPABILITY) {
            return Capabilities.TILE_NETWORK_CAPABILITY.cast(this);
        }

        return super.getCapability(capability, facing);
    }
}
