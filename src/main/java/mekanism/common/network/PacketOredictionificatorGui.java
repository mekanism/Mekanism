package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.TileNetworkList;
import mekanism.client.gui.GuiOredictionificator;
import mekanism.client.gui.filter.GuiOredictionificatorFilter;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.inventory.container.ContainerFilter;
import mekanism.common.inventory.container.ContainerOredictionificator;
import mekanism.common.tile.TileEntityOredictionificator;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class PacketOredictionificatorGui {

    private OredictionificatorGuiPacket packetType;
    private Coord4D coord4D;
    private int guiType;
    private int windowId = -1;
    private int index = -1;

    public PacketOredictionificatorGui(OredictionificatorGuiPacket type, Coord4D coord, int guiID, int extra, int extra2) {
        packetType = type;
        coord4D = coord;
        guiType = guiID;

        if (packetType == OredictionificatorGuiPacket.CLIENT) {
            windowId = extra;
        } else if (packetType == OredictionificatorGuiPacket.SERVER_INDEX) {
            index = extra;
        } else if (packetType == OredictionificatorGuiPacket.CLIENT_INDEX) {
            windowId = extra;
            index = extra2;
        }
    }

    public static void handle(PacketOredictionificatorGui message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            if (!player.world.isRemote) {
                World worldServer = ServerLifecycleHooks.getCurrentServer().getWorld(message.coord4D.dimension);
                if (message.coord4D.getTileEntity(worldServer) instanceof TileEntityOredictionificator) {
                    openServerGui(message.packetType, message.guiType, worldServer, (ServerPlayerEntity) player, message.coord4D, message.index);
                }
            } else if (message.coord4D.getTileEntity(player.world) instanceof TileEntityOredictionificator) {
                try {
                    if (message.packetType == OredictionificatorGuiPacket.CLIENT) {
                        Minecraft.getInstance().displayGuiScreen(getGui(message.packetType, message.guiType, player, player.world, message.coord4D.getPos(), -1));
                    } else if (message.packetType == OredictionificatorGuiPacket.CLIENT_INDEX) {
                        Minecraft.getInstance().displayGuiScreen(getGui(message.packetType, message.guiType, player, player.world, message.coord4D.getPos(), message.index));
                    }
                    player.openContainer.windowId = message.windowId;
                } catch (Exception e) {
                    Mekanism.logger.error("FIXME: Packet handling error", e);
                }
            }
        }, player);
    }

    public static void encode(PacketOredictionificatorGui pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        pkt.coord4D.write(buf);
        buf.writeInt(pkt.guiType);
        if (pkt.packetType == OredictionificatorGuiPacket.CLIENT || pkt.packetType == OredictionificatorGuiPacket.CLIENT_INDEX) {
            buf.writeInt(pkt.windowId);
        }
        if (pkt.packetType == OredictionificatorGuiPacket.SERVER_INDEX || pkt.packetType == OredictionificatorGuiPacket.CLIENT_INDEX) {
            buf.writeInt(pkt.index);
        }
    }

    public static PacketOredictionificatorGui decode(PacketBuffer buf) {
        OredictionificatorGuiPacket packetType = buf.readEnumValue(OredictionificatorGuiPacket.class);
        Coord4D coord4D = Coord4D.read(buf);
        int guiType = buf.readInt();
        int windowId = -1;
        int index = -1;
        if (packetType == OredictionificatorGuiPacket.CLIENT || packetType == OredictionificatorGuiPacket.CLIENT_INDEX) {
            windowId = buf.readInt();
        }
        if (packetType == OredictionificatorGuiPacket.SERVER_INDEX || packetType == OredictionificatorGuiPacket.CLIENT_INDEX) {
            index = buf.readInt();
        }
        int extra = 0;
        int extra2 = 0;
        if (packetType == OredictionificatorGuiPacket.CLIENT) {
            extra = windowId;
        } else if (packetType == OredictionificatorGuiPacket.SERVER_INDEX) {
            extra = index;
        } else if (packetType == OredictionificatorGuiPacket.CLIENT_INDEX) {
            extra2 = index;
        }
        //TODO: Evaluate this
        return new PacketOredictionificatorGui(packetType, coord4D, guiType, extra, extra2);
    }

    public static void openServerGui(OredictionificatorGuiPacket t, int guiType, World world, ServerPlayerEntity playerMP, Coord4D obj, int i) {
        Container container = null;
        playerMP.closeContainer();
        if (guiType == 0) {
            container = new ContainerOredictionificator(playerMP.inventory, (TileEntityOredictionificator) obj.getTileEntity(world));
        } else if (guiType == 1) {
            container = new ContainerFilter(playerMP.inventory, (TileEntityMekanism) obj.getTileEntity(world));
        }
        playerMP.getNextWindowId();
        int window = playerMP.currentWindowId;

        if (t == OredictionificatorGuiPacket.SERVER) {
            Mekanism.packetHandler.sendTo(new PacketOredictionificatorGui(OredictionificatorGuiPacket.CLIENT, obj, guiType, window, 0), playerMP);
        } else if (t == OredictionificatorGuiPacket.SERVER_INDEX) {
            Mekanism.packetHandler.sendTo(new PacketOredictionificatorGui(OredictionificatorGuiPacket.CLIENT_INDEX, obj, guiType, window, i), playerMP);
        }

        playerMP.openContainer = container;
        playerMP.openContainer.windowId = window;
        playerMP.openContainer.addListener(playerMP);

        if (guiType == 0) {
            TileEntityOredictionificator tile = (TileEntityOredictionificator) obj.getTileEntity(world);
            for (PlayerEntity player : tile.playersUsing) {
                Mekanism.packetHandler.sendTo(new PacketTileEntity(obj, tile.getFilterPacket(new TileNetworkList())), (ServerPlayerEntity) player);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static Screen getGui(OredictionificatorGuiPacket packetType, int type, PlayerEntity player, World world, BlockPos pos, int index) {
        if (type == 0) {
            return new GuiOredictionificator(player.inventory, (TileEntityOredictionificator) world.getTileEntity(pos));
        } else if (packetType == OredictionificatorGuiPacket.CLIENT) {
            if (type == 1) {
                return new GuiOredictionificatorFilter(player, (TileEntityOredictionificator) world.getTileEntity(pos));
            }
        } else if (packetType == OredictionificatorGuiPacket.CLIENT_INDEX) {
            if (type == 1) {
                return new GuiOredictionificatorFilter(player, (TileEntityOredictionificator) world.getTileEntity(pos), index);
            }
        }
        return null;
    }

    public enum OredictionificatorGuiPacket {
        SERVER,
        CLIENT,
        SERVER_INDEX,
        CLIENT_INDEX
    }
}