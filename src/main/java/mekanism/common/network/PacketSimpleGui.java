package mekanism.common.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.PacketHandler;
import mekanism.common.base.IGuiProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

//TODO: Is this even needed anymore
public class PacketSimpleGui {

    public static List<IGuiProvider> handlers = new ArrayList<>();

    private Coord4D coord4D;
    private int guiHandler;
    private int windowId;
    private int guiId;

    public PacketSimpleGui(Coord4D coord, int handler, int gui) {
        coord4D = coord;
        guiHandler = handler;
        guiId = gui;
    }

    public PacketSimpleGui(Coord4D coord, int handler, int gui, int id) {
        this(coord, handler, gui);
        windowId = id;
    }

    public static void handle(PacketSimpleGui message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        PacketHandler.handlePacket(() -> {
            if (!player.world.isRemote) {
                World worldServer = ServerLifecycleHooks.getCurrentServer().getWorld(message.coord4D.dimension);
                if (message.coord4D.getTileEntity(worldServer) instanceof TileEntityMekanism) {
                    if (message.guiId == -1) {
                        return;
                    }
                    openServerGui(message.guiHandler, message.guiId, (ServerPlayerEntity) player, player.world, message.coord4D);
                }
            } else {
                Minecraft.getInstance().displayGuiScreen(getGui(message.guiHandler, message.guiId, player, player.world, message.coord4D));
                player.openContainer.windowId = message.windowId;
            }
        }, player);
    }

    public static void encode(PacketSimpleGui pkt, PacketBuffer buf) {
        pkt.coord4D.write(buf);
        buf.writeInt(pkt.guiHandler);
        buf.writeInt(pkt.guiId);
        buf.writeInt(pkt.windowId);
    }

    public static PacketSimpleGui decode(PacketBuffer buf) {
        return new PacketSimpleGui(Coord4D.read(buf), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void openServerGui(int handler, int id, ServerPlayerEntity playerMP, World world, Coord4D obj) {
        playerMP.closeContainer();
        playerMP.getNextWindowId();
        int window = playerMP.currentWindowId;
        Mekanism.packetHandler.sendTo(new PacketSimpleGui(obj, handler, id, window), playerMP);
        playerMP.openContainer = handlers.get(handler).getServerGui(id, playerMP, world, obj.getPos());
        playerMP.openContainer.windowId = window;
        playerMP.openContainer.addListener(playerMP);
    }

    @OnlyIn(Dist.CLIENT)
    public static Screen getGui(int handler, int id, PlayerEntity player, World world, Coord4D obj) {
        return (Screen) handlers.get(handler).getClientGui(id, player, world, obj.getPos());
    }
}