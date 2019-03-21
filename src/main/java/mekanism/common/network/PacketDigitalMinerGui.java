package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import mekanism.api.Coord4D;
import mekanism.client.gui.GuiDigitalMiner;
import mekanism.client.gui.GuiDigitalMinerConfig;
import mekanism.client.gui.filter.GuiMFilterSelect;
import mekanism.client.gui.filter.GuiMItemStackFilter;
import mekanism.client.gui.filter.GuiMMaterialFilter;
import mekanism.client.gui.filter.GuiMModIDFilter;
import mekanism.client.gui.filter.GuiMOreDictFilter;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.api.TileNetworkList;
import mekanism.common.inventory.container.ContainerDigitalMiner;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerNull;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.tile.TileEntityDigitalMiner;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketDigitalMinerGui implements IMessageHandler<DigitalMinerGuiMessage, IMessage> {

    @Override
    public IMessage onMessage(DigitalMinerGuiMessage message, MessageContext context) {
        EntityPlayer player = PacketHandler.getPlayer(context);

        PacketHandler.handlePacket(() ->
        {
            if (!player.world.isRemote) {
                World worldServer = FMLCommonHandler.instance().getMinecraftServerInstance()
                      .getWorld(message.coord4D.dimensionId);

                if (message.coord4D.getTileEntity(worldServer) instanceof TileEntityDigitalMiner) {
                    DigitalMinerGuiMessage
                          .openServerGui(message.packetType, message.guiType, worldServer, (EntityPlayerMP) player,
                                message.coord4D, message.index);
                }
            } else {
                if (message.coord4D.getTileEntity(player.world) instanceof TileEntityDigitalMiner) {
                    try {
                        if (message.packetType == MinerGuiPacket.CLIENT) {
                            FMLCommonHandler.instance().showGuiScreen(DigitalMinerGuiMessage
                                  .getGui(message.packetType, message.guiType, player, player.world,
                                        message.coord4D.getPos(), -1));
                        } else if (message.packetType == MinerGuiPacket.CLIENT_INDEX) {
                            FMLCommonHandler.instance().showGuiScreen(DigitalMinerGuiMessage
                                  .getGui(message.packetType, message.guiType, player, player.world,
                                        message.coord4D.getPos(), message.index));
                        }

                        player.openContainer.windowId = message.windowId;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, player);

        return null;
    }

    public enum MinerGuiPacket {
        SERVER, CLIENT, SERVER_INDEX, CLIENT_INDEX
    }

    public static class DigitalMinerGuiMessage implements IMessage {

        public Coord4D coord4D;

        public MinerGuiPacket packetType;

        public int guiType;

        public int windowId = -1;

        public int index = -1;

        public DigitalMinerGuiMessage() {
        }

        public DigitalMinerGuiMessage(MinerGuiPacket type, Coord4D coord, int guiID, int extra, int extra2) {
            packetType = type;

            coord4D = coord;
            guiType = guiID;

            if (packetType == MinerGuiPacket.CLIENT) {
                windowId = extra;
            } else if (packetType == MinerGuiPacket.SERVER_INDEX) {
                index = extra;
            } else if (packetType == MinerGuiPacket.CLIENT_INDEX) {
                windowId = extra;
                index = extra2;
            }
        }

        public static void openServerGui(MinerGuiPacket t, int guiType, World world, EntityPlayerMP playerMP,
              Coord4D obj, int i) {
            Container container;

            playerMP.closeContainer();

            switch (guiType) {
                case 0:
                    container = new ContainerNull(playerMP, (TileEntityContainerBlock) obj.getTileEntity(world));
                    break;
                case 4:
                    container = new ContainerDigitalMiner(playerMP.inventory,
                          (TileEntityDigitalMiner) obj.getTileEntity(world));
                    break;
                case 5:
                    container = new ContainerNull(playerMP, (TileEntityContainerBlock) obj.getTileEntity(world));
                    break;
//				case 1:
//				case 2:
//				case 3:
//				case 6:
                default:
                    container = new ContainerFilter(playerMP.inventory,
                          (TileEntityContainerBlock) obj.getTileEntity(world));
                    break;
            }

            playerMP.getNextWindowId();
            int window = playerMP.currentWindowId;

            if (t == MinerGuiPacket.SERVER) {
                Mekanism.packetHandler
                      .sendTo(new DigitalMinerGuiMessage(MinerGuiPacket.CLIENT, obj, guiType, window, 0), playerMP);
            } else if (t == MinerGuiPacket.SERVER_INDEX) {
                Mekanism.packetHandler
                      .sendTo(new DigitalMinerGuiMessage(MinerGuiPacket.CLIENT_INDEX, obj, guiType, window, i),
                            playerMP);
            }

            playerMP.openContainer = container;
            playerMP.openContainer.windowId = window;
            playerMP.openContainer.addListener(playerMP);

            if (guiType == 0) {
                TileEntityDigitalMiner tile = (TileEntityDigitalMiner) obj.getTileEntity(world);

                for (EntityPlayer player : tile.playersUsing) {
                    Mekanism.packetHandler
                          .sendTo(new TileEntityMessage(obj, tile.getFilterPacket(new TileNetworkList())),
                                (EntityPlayerMP) player);
                }
            }
        }

        @SideOnly(Side.CLIENT)
        public static GuiScreen getGui(MinerGuiPacket packetType, int type, EntityPlayer player, World world,
              BlockPos pos, int index) {
            if (type == 0) {
                return new GuiDigitalMinerConfig(player, (TileEntityDigitalMiner) world.getTileEntity(pos));
            } else if (type == 4) {
                return new GuiDigitalMiner(player.inventory, (TileEntityDigitalMiner) world.getTileEntity(pos));
            } else if (type == 5) {
                return new GuiMFilterSelect(player, (TileEntityDigitalMiner) world.getTileEntity(pos));
            } else {
                if (packetType == MinerGuiPacket.CLIENT) {
                    if (type == 1) {
                        return new GuiMItemStackFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos));
                    } else if (type == 2) {
                        return new GuiMOreDictFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos));
                    } else if (type == 3) {
                        return new GuiMMaterialFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos));
                    } else if (type == 6) {
                        return new GuiMModIDFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos));
                    }
                } else if (packetType == MinerGuiPacket.CLIENT_INDEX) {
                    if (type == 1) {
                        return new GuiMItemStackFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos),
                              index);
                    } else if (type == 2) {
                        return new GuiMOreDictFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos), index);
                    } else if (type == 3) {
                        return new GuiMMaterialFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos), index);
                    } else if (type == 6) {
                        return new GuiMModIDFilter(player, (TileEntityDigitalMiner) world.getTileEntity(pos), index);
                    }
                }
            }

            return null;
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeInt(packetType.ordinal());

            coord4D.write(dataStream);

            dataStream.writeInt(guiType);

            if (packetType == MinerGuiPacket.CLIENT || packetType == MinerGuiPacket.CLIENT_INDEX) {
                dataStream.writeInt(windowId);
            }

            if (packetType == MinerGuiPacket.SERVER_INDEX || packetType == MinerGuiPacket.CLIENT_INDEX) {
                dataStream.writeInt(index);
            }
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            packetType = MinerGuiPacket.values()[dataStream.readInt()];

            coord4D = Coord4D.read(dataStream);

            guiType = dataStream.readInt();

            if (packetType == MinerGuiPacket.CLIENT || packetType == MinerGuiPacket.CLIENT_INDEX) {
                windowId = dataStream.readInt();
            }

            if (packetType == MinerGuiPacket.SERVER_INDEX || packetType == MinerGuiPacket.CLIENT_INDEX) {
                index = dataStream.readInt();
            }
        }
    }
}
