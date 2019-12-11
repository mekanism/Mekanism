package mekanism.common.network;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.security.ISecurityItem;
import mekanism.common.security.ISecurityTile;
import mekanism.common.security.ISecurityTile.SecurityMode;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketSecurityMode {

    private SecurityPacketType packetType;
    private Coord4D coord4D;
    private Hand currentHand;
    private SecurityMode value;

    public PacketSecurityMode(Coord4D coord, SecurityMode control) {
        packetType = SecurityPacketType.BLOCK;
        coord4D = coord;
        value = control;
    }

    public PacketSecurityMode(Hand hand, SecurityMode control) {
        packetType = SecurityPacketType.ITEM;
        currentHand = hand;
        value = control;
    }

    public static void handle(PacketSecurityMode message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> {
            if (message.packetType == SecurityPacketType.BLOCK) {
                TileEntity tile = MekanismUtils.getTileEntity(player.world, message.coord4D.getPos());
                if (tile instanceof ISecurityTile && ((ISecurityTile) tile).hasSecurity()) {
                    UUID owner = ((ISecurityTile) tile).getSecurity().getOwnerUUID();
                    if (owner != null && player.getUniqueID().equals(owner)) {
                        ((ISecurityTile) tile).getSecurity().setMode(message.value);
                        tile.markDirty();
                    }
                }
            } else {
                ItemStack stack = player.getHeldItem(message.currentHand);
                if (stack.getItem() instanceof ISecurityItem) {
                    ((ISecurityItem) stack.getItem()).setSecurity(stack, message.value);
                }
            }
        });
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketSecurityMode pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.packetType);
        buf.writeEnumValue(pkt.value);
        if (pkt.packetType == SecurityPacketType.BLOCK) {
            pkt.coord4D.write(buf);
        } else {
            buf.writeEnumValue(pkt.currentHand);
        }
    }

    public static PacketSecurityMode decode(PacketBuffer buf) {
        SecurityPacketType packetType = buf.readEnumValue(SecurityPacketType.class);
        SecurityMode mode = buf.readEnumValue(SecurityMode.class);
        if (packetType == SecurityPacketType.BLOCK) {
            return new PacketSecurityMode(Coord4D.read(buf), mode);
        }
        return new PacketSecurityMode(buf.readEnumValue(Hand.class), mode);
    }

    public enum SecurityPacketType {
        BLOCK,
        ITEM
    }
}