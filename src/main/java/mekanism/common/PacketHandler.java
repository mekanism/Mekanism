package mekanism.common;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import javax.annotation.Nonnull;
import mekanism.api.Range4D;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketBoxBlacklist;
import mekanism.common.network.PacketBoxBlacklist.BoxBlacklistMessage;
import mekanism.common.network.PacketConfigSync;
import mekanism.common.network.PacketConfigSync.ConfigSyncMessage;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationUpdateMessage;
import mekanism.common.network.PacketContainerEditMode;
import mekanism.common.network.PacketContainerEditMode.ContainerEditModeMessage;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketDataRequest.DataRequestMessage;
import mekanism.common.network.PacketDigitalMinerGui;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketDropperUse;
import mekanism.common.network.PacketDropperUse.DropperUseMessage;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketEditFilter.EditFilterMessage;
import mekanism.common.network.PacketEntityMove;
import mekanism.common.network.PacketEntityMove.EntityMoveMessage;
import mekanism.common.network.PacketFlamethrowerData;
import mekanism.common.network.PacketFlamethrowerData.FlamethrowerDataMessage;
import mekanism.common.network.PacketFreeRunnerData;
import mekanism.common.network.PacketItemStack;
import mekanism.common.network.PacketItemStack.ItemStackMessage;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketJetpackData.JetpackDataMessage;
import mekanism.common.network.PacketKey;
import mekanism.common.network.PacketKey.KeyMessage;
import mekanism.common.network.PacketLogisticalSorterGui;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.network.PacketOredictionificatorGui;
import mekanism.common.network.PacketOredictionificatorGui.OredictionificatorGuiMessage;
import mekanism.common.network.PacketPersonalChest;
import mekanism.common.network.PacketPersonalChest.PersonalChestMessage;
import mekanism.common.network.PacketPortableTeleporter;
import mekanism.common.network.PacketPortableTeleporter.PortableTeleporterMessage;
import mekanism.common.network.PacketPortalFX;
import mekanism.common.network.PacketPortalFX.PortalFXMessage;
import mekanism.common.network.PacketRedstoneControl;
import mekanism.common.network.PacketRedstoneControl.RedstoneControlMessage;
import mekanism.common.network.PacketRemoveUpgrade;
import mekanism.common.network.PacketRemoveUpgrade.RemoveUpgradeMessage;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketRobit.RobitMessage;
import mekanism.common.network.PacketScubaTankData;
import mekanism.common.network.PacketScubaTankData.ScubaTankDataMessage;
import mekanism.common.network.PacketSecurityMode;
import mekanism.common.network.PacketSecurityMode.SecurityModeMessage;
import mekanism.common.network.PacketSecurityUpdate;
import mekanism.common.network.PacketSecurityUpdate.SecurityUpdateMessage;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.network.PacketSimpleGui.SimpleGuiMessage;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.network.PacketTransmitterUpdate;
import mekanism.common.network.PacketTransmitterUpdate.TransmitterUpdateMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Mekanism packet handler. As always, use packets sparingly!
 *
 * @author AidanBrady
 */
public class PacketHandler {

    public SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel("MEK");

    /**
     * Encodes an Object[] of data into a DataOutputStream.
     *
     * @param dataValues - an Object[] of data to encode
     * @param output     - the output stream to write to
     */
    public static void encode(Object[] dataValues, ByteBuf output) {
        try {
            for (Object data : dataValues) {
                if (data instanceof Byte) {
                    output.writeByte((Byte) data);
                } else if (data instanceof Integer) {
                    output.writeInt((Integer) data);
                } else if (data instanceof Short) {
                    output.writeShort((Short) data);
                } else if (data instanceof Long) {
                    output.writeLong((Long) data);
                } else if (data instanceof Boolean) {
                    output.writeBoolean((Boolean) data);
                } else if (data instanceof Double) {
                    output.writeDouble((Double) data);
                } else if (data instanceof Float) {
                    output.writeFloat((Float) data);
                } else if (data instanceof String) {
                    writeString(output, (String) data);
                } else if (data instanceof EnumFacing) {
                    output.writeInt(((EnumFacing) data).ordinal());
                } else if (data instanceof ItemStack) {
                    writeStack(output, (ItemStack) data);
                } else if (data instanceof NBTTagCompound) {
                    writeNBT(output, (NBTTagCompound) data);
                } else if (data instanceof int[]) {
                    for (int i : (int[]) data) {
                        output.writeInt(i);
                    }
                } else if (data instanceof byte[]) {
                    for (byte b : (byte[]) data) {
                        output.writeByte(b);
                    }
                } else if (data instanceof ArrayList) {
                    encode(((ArrayList<?>) data).toArray(), output);
                } else if (data instanceof NonNullList) {
                    encode(((NonNullList) data).toArray(), output);
                } else {
                    throw new RuntimeException("Un-encodable data passed to encode(): " + data + ", full data: " + Arrays.toString(dataValues));
                }
            }
        } catch (Exception e) {
            Mekanism.logger.error("Error while encoding packet data.", e);
        }
    }

    public static void writeString(ByteBuf output, String s) {
        ByteBufUtils.writeUTF8String(output, s);
    }

    public static String readString(ByteBuf input) {
        return ByteBufUtils.readUTF8String(input);
    }

    public static void writeStack(ByteBuf output, ItemStack stack) {
        ByteBufUtils.writeItemStack(output, stack);
    }

    public static ItemStack readStack(ByteBuf input) {
        return ByteBufUtils.readItemStack(input);
    }

    public static void writeNBT(ByteBuf output, NBTTagCompound nbtTags) {
        ByteBufUtils.writeTag(output, nbtTags);
    }

    public static NBTTagCompound readNBT(ByteBuf input) {
        return ByteBufUtils.readTag(input);
    }

    public static void log(String log) {
        if (MekanismConfig.current().general.logPackets.val()) {
            Mekanism.logger.info(log);
        }
    }

    public static EntityPlayer getPlayer(MessageContext context) {
        return Mekanism.proxy.getPlayer(context);
    }

    public static void handlePacket(Runnable runnable, EntityPlayer player) {
        Mekanism.proxy.handlePacket(runnable, player);
    }

    public void initialize() {
        netHandler.registerMessage(PacketRobit.class, RobitMessage.class, 0, Side.SERVER);
        netHandler.registerMessage(PacketTransmitterUpdate.class, TransmitterUpdateMessage.class, 1, Side.CLIENT);
        netHandler.registerMessage(PacketPersonalChest.class, PersonalChestMessage.class, 2, Side.CLIENT);
        netHandler.registerMessage(PacketPersonalChest.class, PersonalChestMessage.class, 2, Side.SERVER);
        //FREE ID 3
        netHandler.registerMessage(PacketItemStack.class, ItemStackMessage.class, 4, Side.SERVER);
        netHandler.registerMessage(PacketTileEntity.class, TileEntityMessage.class, 5, Side.CLIENT);
        netHandler.registerMessage(PacketTileEntity.class, TileEntityMessage.class, 5, Side.SERVER);
        netHandler.registerMessage(PacketPortalFX.class, PortalFXMessage.class, 6, Side.CLIENT);
        netHandler.registerMessage(PacketDataRequest.class, DataRequestMessage.class, 7, Side.SERVER);
        netHandler.registerMessage(PacketOredictionificatorGui.class, OredictionificatorGuiMessage.class, 8, Side.CLIENT);
        netHandler.registerMessage(PacketOredictionificatorGui.class, OredictionificatorGuiMessage.class, 8, Side.SERVER);
        netHandler.registerMessage(PacketSecurityMode.class, SecurityModeMessage.class, 9, Side.SERVER);
        netHandler.registerMessage(PacketPortableTeleporter.class, PortableTeleporterMessage.class, 10, Side.CLIENT);
        netHandler.registerMessage(PacketPortableTeleporter.class, PortableTeleporterMessage.class, 10, Side.SERVER);
        netHandler.registerMessage(PacketRemoveUpgrade.class, RemoveUpgradeMessage.class, 11, Side.SERVER);
        netHandler.registerMessage(PacketRedstoneControl.class, RedstoneControlMessage.class, 12, Side.SERVER);
        //FREE ID 13
        netHandler.registerMessage(PacketLogisticalSorterGui.class, LogisticalSorterGuiMessage.class, 14, Side.CLIENT);
        netHandler.registerMessage(PacketLogisticalSorterGui.class, LogisticalSorterGuiMessage.class, 14, Side.SERVER);
        netHandler.registerMessage(PacketNewFilter.class, NewFilterMessage.class, 15, Side.SERVER);
        netHandler.registerMessage(PacketEditFilter.class, EditFilterMessage.class, 16, Side.SERVER);
        netHandler.registerMessage(PacketConfigurationUpdate.class, ConfigurationUpdateMessage.class, 17, Side.SERVER);
        netHandler.registerMessage(PacketSimpleGui.class, SimpleGuiMessage.class, 18, Side.CLIENT);
        netHandler.registerMessage(PacketSimpleGui.class, SimpleGuiMessage.class, 18, Side.SERVER);
        netHandler.registerMessage(PacketDigitalMinerGui.class, DigitalMinerGuiMessage.class, 19, Side.CLIENT);
        netHandler.registerMessage(PacketDigitalMinerGui.class, DigitalMinerGuiMessage.class, 19, Side.SERVER);
        netHandler.registerMessage(PacketJetpackData.class, JetpackDataMessage.class, 20, Side.CLIENT);
        netHandler.registerMessage(PacketJetpackData.class, JetpackDataMessage.class, 20, Side.SERVER);
        netHandler.registerMessage(PacketKey.class, KeyMessage.class, 21, Side.SERVER);
        netHandler.registerMessage(PacketScubaTankData.class, ScubaTankDataMessage.class, 22, Side.CLIENT);
        netHandler.registerMessage(PacketScubaTankData.class, ScubaTankDataMessage.class, 22, Side.SERVER);
        netHandler.registerMessage(PacketConfigSync.class, ConfigSyncMessage.class, 23, Side.CLIENT);
        netHandler.registerMessage(PacketBoxBlacklist.class, BoxBlacklistMessage.class, 24, Side.CLIENT);
        //FREE ID 25
        netHandler.registerMessage(PacketContainerEditMode.class, ContainerEditModeMessage.class, 26, Side.SERVER);
        netHandler.registerMessage(PacketFlamethrowerData.class, FlamethrowerDataMessage.class, 27, Side.CLIENT);
        netHandler.registerMessage(PacketFlamethrowerData.class, FlamethrowerDataMessage.class, 27, Side.SERVER);
        netHandler.registerMessage(PacketDropperUse.class, DropperUseMessage.class, 28, Side.SERVER);
        netHandler.registerMessage(PacketEntityMove.class, EntityMoveMessage.class, 29, Side.CLIENT);
        netHandler.registerMessage(PacketSecurityUpdate.class, SecurityUpdateMessage.class, 30, Side.CLIENT);
        netHandler.registerMessage(PacketFreeRunnerData.class, PacketFreeRunnerData.FreeRunnerDataMessage.class, 31, Side.CLIENT);
        netHandler.registerMessage(PacketFreeRunnerData.class, PacketFreeRunnerData.FreeRunnerDataMessage.class, 31, Side.SERVER);
    }

    /**
     * Send this message to the specified player.
     *
     * @param message - the message to send
     * @param player  - the player to send it to
     */
    public void sendTo(IMessage message, EntityPlayerMP player) {
        netHandler.sendTo(message, player);
    }

    /**
     * Send this message to everyone connected to the server.
     *
     * @param message - message to send
     */
    public void sendToAll(IMessage message) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            sendTo(message, player);
        }
    }

    /**
     * Send this message to everyone within a certain range of a point.
     *
     * @param message - the message to send
     * @param point   - the TargetPoint around which to send
     */
    public void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
        netHandler.sendToAllAround(message, point);
    }

    /**
     * Send this message to everyone within the supplied dimension.
     *
     * @param message     - the message to send
     * @param dimensionId - the dimension id to target
     */
    public void sendToDimension(IMessage message, int dimensionId) {
        netHandler.sendToDimension(message, dimensionId);
    }

    /**
     * Send this message to the server.
     *
     * @param message - the message to send
     */
    public void sendToServer(IMessage message) {
        netHandler.sendToServer(message);
    }

    /**
     * Send this message to all players within a defined AABB cuboid.
     *
     * @param message - the message to send
     * @param cuboid  - the AABB cuboid to send the packet in
     * @param dimId   - the dimension the cuboid is in
     */
    public void sendToCuboid(IMessage message, AxisAlignedBB cuboid, int dimId) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null && cuboid != null) {
            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                if (player.dimension == dimId && cuboid.contains(new Vec3d(player.posX, player.posY, player.posZ))) {
                    sendTo(message, player);
                }
            }
        }
    }

    public void sendToReceivers(IMessage message, Range4D range) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) {
            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                if (player.dimension == range.dimensionId && Range4D.getChunkRange(player).intersects(range)) {
                    sendTo(message, player);
                }
            }
        }
    }

    @Nonnull
    public static UUID readUUID(ByteBuf dataStream) {
        return new UUID(dataStream.readLong(), dataStream.readLong());
    }

    public static void writeUUID(ByteBuf dataStream, UUID uuid) {
        dataStream.writeLong(uuid.getMostSignificantBits());
        dataStream.writeLong(uuid.getLeastSignificantBits());
    }
}