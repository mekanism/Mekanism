package mekanism.common.network;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.client.gui.GuiLogisticalSorter;
import mekanism.client.gui.filter.GuiTFilterSelect;
import mekanism.client.gui.filter.GuiTItemStackFilter;
import mekanism.client.gui.filter.GuiTMaterialFilter;
import mekanism.client.gui.filter.GuiTModIDFilter;
import mekanism.client.gui.filter.GuiTOreDictFilter;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.inventory.container.tile.filter.list.LSFilterListContainer;
import mekanism.common.tile.TileEntityLogisticalSorter;
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

//TODO: Fix
public class PacketLogisticalSorterGui {

    private SorterGuiPacket packetType;
    private Coord4D coord4D;
    private int guiType;
    private int windowId = -1;
    private int index = -1;

    public PacketLogisticalSorterGui(SorterGuiPacket type, Coord4D coord, int guiId, int extra, int extra2) {
        packetType = type;

        coord4D = coord;
        guiType = guiId;

        if (packetType == SorterGuiPacket.CLIENT) {
            windowId = extra;
        } else if (packetType == SorterGuiPacket.SERVER_INDEX) {
            index = extra;
        } else if (packetType == SorterGuiPacket.CLIENT_INDEX) {
            windowId = extra2;
            index = extra2;
        }
    }

    public static void handle(PacketLogisticalSorterGui message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            if (!player.world.isRemote) {
                World worldServer = ServerLifecycleHooks.getCurrentServer().getWorld(message.coord4D.dimension);
                if (message.coord4D.getTileEntity(worldServer) instanceof TileEntityLogisticalSorter) {
                    openServerGui(message.packetType, message.guiType, worldServer, (ServerPlayerEntity) player, message.coord4D, message.index);
                }
            } else if (message.coord4D.getTileEntity(player.world) instanceof TileEntityLogisticalSorter) {
                try {
                    if (message.packetType == SorterGuiPacket.CLIENT) {
                        Minecraft.getInstance().displayGuiScreen(PacketLogisticalSorterGui.getGui(message.packetType, message.guiType, player, player.world,
                              message.coord4D.getPos(), -1));
                    } else if (message.packetType == SorterGuiPacket.CLIENT_INDEX) {
                        Minecraft.getInstance().displayGuiScreen(PacketLogisticalSorterGui.getGui(message.packetType, message.guiType, player, player.world,
                              message.coord4D.getPos(), message.index));
                    }
                    player.openContainer.windowId = message.windowId;
                } catch (Exception e) {
                    Mekanism.logger.error("FIXME: Packet handling error", e);
                }
            }
        }, player);
    }

    public static void encode(PacketLogisticalSorterGui pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        pkt.coord4D.write(buf);
        buf.writeInt(pkt.guiType);
        if (pkt.packetType == SorterGuiPacket.CLIENT || pkt.packetType == SorterGuiPacket.CLIENT_INDEX) {
            buf.writeInt(pkt.windowId);
        }
        if (pkt.packetType == SorterGuiPacket.SERVER_INDEX || pkt.packetType == SorterGuiPacket.CLIENT_INDEX) {
            buf.writeInt(pkt.index);
        }
    }

    public static PacketLogisticalSorterGui decode(PacketBuffer buf) {
        SorterGuiPacket packetType = buf.readEnumValue(SorterGuiPacket.class);
        Coord4D coord4D = Coord4D.read(buf);
        int guiType = buf.readInt();
        int windowId = -1;
        int index = -1;
        if (packetType == SorterGuiPacket.CLIENT || packetType == SorterGuiPacket.CLIENT_INDEX) {
            windowId = buf.readInt();
        }
        if (packetType == SorterGuiPacket.SERVER_INDEX || packetType == SorterGuiPacket.CLIENT_INDEX) {
            index = buf.readInt();
        }
        int extra = 0;
        int extra2 = 0;
        if (packetType == SorterGuiPacket.CLIENT) {
            extra = windowId;
        } else if (packetType == SorterGuiPacket.SERVER_INDEX) {
            extra = index;
        } else if (packetType == SorterGuiPacket.CLIENT_INDEX) {
            extra2 = index;
        }
        //TODO: Evaluate this
        return new PacketLogisticalSorterGui(packetType, coord4D, guiType, extra, extra2);
    }

    public static void openServerGui(SorterGuiPacket t, int guiType, World world, ServerPlayerEntity playerMP, Coord4D obj, int i) {
        Container container = null;

        playerMP.closeContainer();

        if (guiType == 0) {
            container = new LSFilterListContainer(playerMP, (TileEntityMekanism) obj.getTileEntity(world));
        } else if (guiType == 4) {
            container = new LSFilterListContainer(playerMP, (TileEntityMekanism) obj.getTileEntity(world));
        } else if (guiType == 1 || guiType == 2 || guiType == 3 || guiType == 5) {
            container = new FilterContainer<TileEntityLogisticalSorter>(playerMP.inventory, (TileEntityMekanism) obj.getTileEntity(world));
        }
        playerMP.getNextWindowId();
        int window = playerMP.currentWindowId;
        if (t == SorterGuiPacket.SERVER) {
            Mekanism.packetHandler.sendTo(new PacketLogisticalSorterGui(SorterGuiPacket.CLIENT, obj, guiType, window, 0), playerMP);
        } else if (t == SorterGuiPacket.SERVER_INDEX) {
            Mekanism.packetHandler.sendTo(new PacketLogisticalSorterGui(SorterGuiPacket.CLIENT_INDEX, obj, guiType, window, i), playerMP);
        }
        playerMP.openContainer = container;
        playerMP.openContainer.windowId = window;
        playerMP.openContainer.addListener(playerMP);
    }

    @OnlyIn(Dist.CLIENT)
    public static Screen getGui(SorterGuiPacket packetType, int type, PlayerEntity player, World world, BlockPos pos, int index) {
        if (type == 0) {
            return new GuiLogisticalSorter(player, (TileEntityLogisticalSorter) world.getTileEntity(pos));
        } else if (type == 4) {
            return new GuiTFilterSelect(player, (TileEntityLogisticalSorter) world.getTileEntity(pos));
        } else if (packetType == SorterGuiPacket.CLIENT) {
            if (type == 1) {
                return new GuiTItemStackFilter(player, (TileEntityLogisticalSorter) world.getTileEntity(pos));
            } else if (type == 2) {
                return new GuiTOreDictFilter(player, (TileEntityLogisticalSorter) world.getTileEntity(pos));
            } else if (type == 3) {
                return new GuiTMaterialFilter(player, (TileEntityLogisticalSorter) world.getTileEntity(pos));
            } else if (type == 5) {
                return new GuiTModIDFilter(player, (TileEntityLogisticalSorter) world.getTileEntity(pos));
            }
        } else if (packetType == SorterGuiPacket.CLIENT_INDEX) {
            if (type == 1) {
                return new GuiTItemStackFilter(player, (TileEntityLogisticalSorter) world.getTileEntity(pos), index);
            } else if (type == 2) {
                return new GuiTOreDictFilter(player, (TileEntityLogisticalSorter) world.getTileEntity(pos), index);
            } else if (type == 3) {
                return new GuiTMaterialFilter(player, (TileEntityLogisticalSorter) world.getTileEntity(pos), index);
            } else if (type == 5) {
                return new GuiTModIDFilter(player, (TileEntityLogisticalSorter) world.getTileEntity(pos), index);
            }
        }
        return null;
    }

    public enum SorterGuiPacket {
        SERVER,
        CLIENT,
        SERVER_INDEX,
        CLIENT_INDEX
    }
}