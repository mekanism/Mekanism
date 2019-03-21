package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.api.TileNetworkList;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator.OredictionificatorFilter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketEditFilter implements IMessageHandler<EditFilterMessage, IMessage> {

    @Override
    public IMessage onMessage(EditFilterMessage message, MessageContext context) {
        WorldServer worldServer = FMLCommonHandler.instance().getMinecraftServerInstance()
              .getWorld(message.coord4D.dimensionId);

        worldServer.addScheduledTask(() ->
        {
            if (message.type == 0 && message.coord4D.getTileEntity(worldServer) instanceof TileEntityLogisticalSorter) {
                TileEntityLogisticalSorter sorter = (TileEntityLogisticalSorter) message.coord4D
                      .getTileEntity(worldServer);

                if (!sorter.filters.contains(message.tFilter)) {
                    return;
                }

                int index = sorter.filters.indexOf(message.tFilter);

                sorter.filters.remove(index);

                if (!message.delete) {
                    sorter.filters.add(index, message.tEdited);
                }

                for (EntityPlayer iterPlayer : sorter.playersUsing) {
                    Mekanism.packetHandler.sendTo(
                          new TileEntityMessage(Coord4D.get(sorter), sorter.getFilterPacket(new TileNetworkList())),
                          (EntityPlayerMP) iterPlayer);
                }
            } else if (message.type == 1 && message.coord4D
                  .getTileEntity(worldServer) instanceof TileEntityDigitalMiner) {
                TileEntityDigitalMiner miner = (TileEntityDigitalMiner) message.coord4D.getTileEntity(worldServer);

                if (!miner.filters.contains(message.mFilter)) {
                    return;
                }

                int index = miner.filters.indexOf(message.mFilter);

                miner.filters.remove(index);

                if (!message.delete) {
                    miner.filters.add(index, message.mEdited);
                }

                for (EntityPlayer iterPlayer : miner.playersUsing) {
                    Mekanism.packetHandler.sendTo(
                          new TileEntityMessage(Coord4D.get(miner), miner.getFilterPacket(new TileNetworkList())),
                          (EntityPlayerMP) iterPlayer);
                }
            } else if (message.type == 2 && message.coord4D
                  .getTileEntity(worldServer) instanceof TileEntityOredictionificator) {
                TileEntityOredictionificator oredictionificator = (TileEntityOredictionificator) message.coord4D
                      .getTileEntity(worldServer);

                if (!oredictionificator.filters.contains(message.oFilter)) {
                    return;
                }

                int index = oredictionificator.filters.indexOf(message.oFilter);

                oredictionificator.filters.remove(index);

                if (!message.delete) {
                    oredictionificator.filters.add(index, message.oEdited);
                }

                for (EntityPlayer iterPlayer : oredictionificator.playersUsing) {
                    Mekanism.packetHandler.sendTo(new TileEntityMessage(Coord4D.get(oredictionificator),
                          oredictionificator.getFilterPacket(new TileNetworkList())), (EntityPlayerMP) iterPlayer);
                }
            }
        });

        return null;
    }

    public static class EditFilterMessage implements IMessage {

        public Coord4D coord4D;

        public TransporterFilter tFilter;
        public TransporterFilter tEdited;

        public MinerFilter mFilter;
        public MinerFilter mEdited;

        public OredictionificatorFilter oFilter;
        public OredictionificatorFilter oEdited;

        public byte type = -1;

        public boolean delete;

        public EditFilterMessage() {
        }

        public EditFilterMessage(Coord4D coord, boolean deletion, Object filter, Object edited) {
            coord4D = coord;
            delete = deletion;

            if (filter instanceof TransporterFilter) {
                tFilter = (TransporterFilter) filter;

                if (!delete) {
                    tEdited = (TransporterFilter) edited;
                }

                type = 0;
            } else if (filter instanceof MinerFilter) {
                mFilter = (MinerFilter) filter;

                if (!delete) {
                    mEdited = (MinerFilter) edited;
                }

                type = 1;
            } else if (filter instanceof OredictionificatorFilter) {
                oFilter = (OredictionificatorFilter) filter;

                if (!delete) {
                    oEdited = (OredictionificatorFilter) edited;
                }

                type = 2;
            }
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            coord4D.write(dataStream);

            dataStream.writeByte(type);

            dataStream.writeBoolean(delete);

            TileNetworkList data = new TileNetworkList();

            if (type == 0) {
                tFilter.write(data);

                if (!delete) {
                    tEdited.write(data);
                }
            } else if (type == 1) {
                mFilter.write(data);

                if (!delete) {
                    mEdited.write(data);
                }
            } else if (type == 2) {
                oFilter.write(data);

                if (!delete) {
                    oEdited.write(data);
                }
            }

            PacketHandler.encode(data.toArray(), dataStream);
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            coord4D = Coord4D.read(dataStream);

            type = dataStream.readByte();
            delete = dataStream.readBoolean();

            if (type == 0) {
                tFilter = TransporterFilter.readFromPacket(dataStream);

                if (!delete) {
                    tEdited = TransporterFilter.readFromPacket(dataStream);
                }
            } else if (type == 1) {
                mFilter = MinerFilter.readFromPacket(dataStream);

                if (!delete) {
                    mEdited = MinerFilter.readFromPacket(dataStream);
                }
            } else if (type == 2) {
                oFilter = OredictionificatorFilter.readFromPacket(dataStream);

                if (!delete) {
                    oEdited = OredictionificatorFilter.readFromPacket(dataStream);
                }
            }
        }
    }
}
