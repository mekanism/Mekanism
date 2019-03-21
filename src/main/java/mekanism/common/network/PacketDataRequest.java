package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.ITileNetwork;
import mekanism.api.TileNetworkList;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketDataRequest implements IMessageHandler<DataRequestMessage, IMessage> {

    @Override
    public IMessage onMessage(DataRequestMessage message, MessageContext context) {
        EntityPlayer player = PacketHandler.getPlayer(context);

        PacketHandler.handlePacket(() ->
        {
            World worldServer = DimensionManager.getWorld(message.coord4D.dimensionId);

            if (worldServer != null) {
                TileEntity tileEntity = message.coord4D.getTileEntity(worldServer);

                if (tileEntity instanceof TileEntityMultiblock) {
                    ((TileEntityMultiblock<?>) tileEntity).sendStructure = true;
                }

                if (CapabilityUtils.hasCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null)) {
                    IGridTransmitter transmitter = CapabilityUtils
                          .getCapability(tileEntity, Capabilities.GRID_TRANSMITTER_CAPABILITY, null);

                    transmitter.setRequestsUpdate();

                    if (transmitter.hasTransmitterNetwork()) {
                        transmitter.getTransmitterNetwork().addUpdate(player);
                    }
                }

                if (CapabilityUtils.hasCapability(tileEntity, Capabilities.TILE_NETWORK_CAPABILITY, null)) {
                    ITileNetwork network = CapabilityUtils
                          .getCapability(tileEntity, Capabilities.TILE_NETWORK_CAPABILITY, null);

                    Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(tileEntity),
                          network.getNetworkedData(new TileNetworkList())), (EntityPlayerMP) player);
                }
            }
        }, player);

        return null;
    }

    public static class DataRequestMessage implements IMessage {

        public Coord4D coord4D;

        public DataRequestMessage() {
        }

        public DataRequestMessage(Coord4D coord) {
            coord4D = coord;
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            coord4D.write(dataStream);
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            coord4D = Coord4D.read(dataStream);
        }
    }
}
