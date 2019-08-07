package mekanism.common;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.Range4D;
import mekanism.common.base.ITileNetwork;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketBoxBlacklist;
import mekanism.common.network.PacketConfigSync;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketContainerEditMode;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketDigitalMinerGui;
import mekanism.common.network.PacketDigitalMinerGui.DigitalMinerGuiMessage;
import mekanism.common.network.PacketDropperUse;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketEntityMove;
import mekanism.common.network.PacketFlamethrowerData;
import mekanism.common.network.PacketFreeRunnerData;
import mekanism.common.network.PacketItemStack;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketKey;
import mekanism.common.network.PacketLogisticalSorterGui;
import mekanism.common.network.PacketLogisticalSorterGui.LogisticalSorterGuiMessage;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.network.PacketNewFilter.NewFilterMessage;
import mekanism.common.network.PacketOredictionificatorGui;
import mekanism.common.network.PacketOredictionificatorGui.OredictionificatorGuiMessage;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * Mekanism packet handler. As always, use packets sparingly!
 *
 * @author AidanBrady
 */
public class PacketHandler {

    private static final String PROTOCOL_VERSION = Integer.toString(1);
    private static final SimpleChannel netHandler = NetworkRegistry.ChannelBuilder
          .named(new ResourceLocation(Mekanism.MODID, "main_channel"))
          .clientAcceptedVersions(PROTOCOL_VERSION::equals)
          .serverAcceptedVersions(PROTOCOL_VERSION::equals)
          .networkProtocolVersion(() -> PROTOCOL_VERSION)
          .simpleChannel();

    /**
     * Encodes an Object[] of data into a DataOutputStream.
     *
     * @param dataValues - an Object[] of data to encode
     * @param output     - the output stream to write to
     */
    public static void encode(Object[] dataValues, PacketBuffer output) {
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
            } else if (data instanceof Direction) {
                output.writeInt(((Direction) data).ordinal());
            } else if (data instanceof ItemStack) {
                writeStack(output, (ItemStack) data);
            } else if (data instanceof CompoundNBT) {
                writeNBT(output, (CompoundNBT) data);
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
    }

    //TODO: Replace these helper read/write things with just direct calls
    public static void writeString(PacketBuffer output, String s) {
        output.writeString(s);
    }

    public static String readString(PacketBuffer input) {
        return input.readString();
    }

    public static void writeStack(PacketBuffer output, ItemStack stack) {
        output.writeItemStack(stack);
    }

    public static ItemStack readStack(PacketBuffer input) {
        return input.readItemStack();
    }

    public static void writeNBT(PacketBuffer output, CompoundNBT nbtTags) {
        output.writeCompoundTag(nbtTags);
    }

    public static CompoundNBT readNBT(PacketBuffer input) {
        return input.readCompoundTag();
    }

    @Nonnull
    public static UUID readUUID(PacketBuffer dataStream) {
        return dataStream.readUniqueId();
    }

    public static void writeUUID(PacketBuffer dataStream, UUID uuid) {
        dataStream.writeUniqueId(uuid);
    }

    public static void log(String log) {
        if (MekanismConfig.current().general.logPackets.val()) {
            Mekanism.logger.info(log);
        }
    }

    public static PlayerEntity getPlayer(Supplier<Context> context) {
        return Mekanism.proxy.getPlayer(context);
    }

    public static void handlePacket(Runnable runnable, PlayerEntity player) {
        Mekanism.proxy.handlePacket(runnable, player);
    }

    public void initialize() {
        int disc = 0;

        netHandler.registerMessage(PacketRobit.class, RobitMessage.class, 0, Dist.DEDICATED_SERVER);
        netHandler.registerMessage(PacketTransmitterUpdate.class, TransmitterUpdateMessage.class, 1, Dist.CLIENT);
        netHandler.registerMessage(disc++, PacketItemStack.class, PacketItemStack::encode, PacketItemStack::decode, PacketItemStack::handle);
        netHandler.registerMessage(PacketTileEntity.class, TileEntityMessage.class, 5, Dist.CLIENT);
        netHandler.registerMessage(PacketTileEntity.class, TileEntityMessage.class, 5, Dist.DEDICATED_SERVER);
        netHandler.registerMessage(PacketPortalFX.class, PortalFXMessage.class, 6, Dist.CLIENT);
        netHandler.registerMessage(disc++, PacketDataRequest.class, PacketDataRequest::encode, PacketDataRequest::decode, PacketDataRequest::handle);
        netHandler.registerMessage(PacketOredictionificatorGui.class, OredictionificatorGuiMessage.class, 8, Dist.CLIENT);
        netHandler.registerMessage(PacketOredictionificatorGui.class, OredictionificatorGuiMessage.class, 8, Dist.DEDICATED_SERVER);
        netHandler.registerMessage(PacketSecurityMode.class, SecurityModeMessage.class, 9, Dist.DEDICATED_SERVER);
        netHandler.registerMessage(PacketPortableTeleporter.class, PortableTeleporterMessage.class, 10, Dist.CLIENT);
        netHandler.registerMessage(PacketPortableTeleporter.class, PortableTeleporterMessage.class, 10, Dist.DEDICATED_SERVER);
        netHandler.registerMessage(PacketRemoveUpgrade.class, RemoveUpgradeMessage.class, 11, Dist.DEDICATED_SERVER);
        netHandler.registerMessage(PacketRedstoneControl.class, RedstoneControlMessage.class, 12, Dist.DEDICATED_SERVER);
        netHandler.registerMessage(PacketLogisticalSorterGui.class, LogisticalSorterGuiMessage.class, 14, Dist.CLIENT);
        netHandler.registerMessage(PacketLogisticalSorterGui.class, LogisticalSorterGuiMessage.class, 14, Dist.DEDICATED_SERVER);
        netHandler.registerMessage(PacketNewFilter.class, NewFilterMessage.class, 15, Dist.DEDICATED_SERVER);
        netHandler.registerMessage(disc++, PacketEditFilter.class, PacketEditFilter::encode, PacketEditFilter::decode, PacketEditFilter::handle);
        netHandler.registerMessage(disc++, PacketConfigurationUpdate.class, PacketConfigurationUpdate::encode, PacketConfigurationUpdate::decode, PacketConfigurationUpdate::handle);
        netHandler.registerMessage(PacketSimpleGui.class, SimpleGuiMessage.class, 18, Dist.CLIENT);
        netHandler.registerMessage(PacketSimpleGui.class, SimpleGuiMessage.class, 18, Dist.DEDICATED_SERVER);
        netHandler.registerMessage(PacketDigitalMinerGui.class, DigitalMinerGuiMessage.class, 19, Dist.CLIENT);
        netHandler.registerMessage(PacketDigitalMinerGui.class, DigitalMinerGuiMessage.class, 19, Dist.DEDICATED_SERVER);
        netHandler.registerMessage(disc++, PacketJetpackData.class, PacketJetpackData::encode, PacketJetpackData::decode, PacketJetpackData::handle);
        netHandler.registerMessage(disc++, PacketKey.class, PacketKey::encode, PacketKey::decode, PacketKey::handle);
        netHandler.registerMessage(PacketScubaTankData.class, ScubaTankDataMessage.class, 22, Dist.CLIENT);
        netHandler.registerMessage(PacketScubaTankData.class, ScubaTankDataMessage.class, 22, Dist.DEDICATED_SERVER);
        netHandler.registerMessage(disc++, PacketConfigSync.class, PacketConfigSync::encode, PacketConfigSync::decode, PacketConfigSync::handle);
        netHandler.registerMessage(disc++, PacketBoxBlacklist.class, PacketBoxBlacklist::encode, PacketBoxBlacklist::decode, PacketBoxBlacklist::handle);
        netHandler.registerMessage(disc++, PacketContainerEditMode.class, PacketContainerEditMode::encode, PacketContainerEditMode::decode, PacketContainerEditMode::handle);
        netHandler.registerMessage(disc++, PacketFlamethrowerData.class, PacketFlamethrowerData::encode, PacketFlamethrowerData::decode, PacketFlamethrowerData::handle);
        netHandler.registerMessage(disc++, PacketDropperUse.class, PacketDropperUse::encode, PacketDropperUse::decode, PacketDropperUse::handle);
        netHandler.registerMessage(disc++, PacketEntityMove.class, PacketEntityMove::encode, PacketEntityMove::decode, PacketEntityMove::handle);
        netHandler.registerMessage(PacketSecurityUpdate.class, SecurityUpdateMessage.class, 30, Dist.CLIENT);
        netHandler.registerMessage(disc++, PacketFreeRunnerData.class, PacketFreeRunnerData::encode, PacketFreeRunnerData::decode, PacketFreeRunnerData::handle);
    }

    /**
     * Send this message to the specified player.
     *
     * @param message - the message to send
     * @param player  - the player to send it to
     */
    public void sendTo(IMessage message, ServerPlayerEntity player) {
        netHandler.sendTo(message, player);
    }

    /**
     * Send this message to everyone connected to the server.
     *
     * @param message - message to send
     */
    public void sendToAll(IMessage message) {
        netHandler.sendToAll(message);
    }

    /**
     * Send this message to everyone within a certain range of a point.
     *
     * @param message - the message to send
     * @param point   - the TargetPoint around which to send
     */
    public void sendToAllAround(IMessage message, TargetPoint point) {
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
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && cuboid != null) {
            for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
                if (player.dimension == dimId && cuboid.contains(new Vec3d(player.posX, player.posY, player.posZ))) {
                    sendTo(message, player);
                }
            }
        }
    }

    public <TILE extends TileEntity & ITileNetwork> void sendUpdatePacket(TILE tile) {
        sendToAllTracking(new TileEntityMessage(tile), tile);
    }

    public void sendToAllTracking(IMessage message, TileEntity tile) {
        BlockPos pos = tile.getPos();
        sendToAllTracking(message, tile.getWorld().provider.getDimension(), pos.getX(), pos.getY(), pos.getZ());
    }

    public void sendToAllTracking(IMessage message, Coord4D point) {
        sendToAllTracking(message, point.dimensionId, point.x, point.y, point.z);
    }

    public void sendToAllTracking(IMessage message, int dimension, double x, double y, double z) {
        //Range is ignored for sendToAllTracking, and only gets sent to clients that have the location loaded
        sendToAllTracking(message, new TargetPoint(dimension, x, y, z, 1));
    }

    public void sendToAllTracking(IMessage message, TargetPoint point) {
        netHandler.sendToAllTracking(message, point);
    }

    //TODO: change Network stuff over to using this
    public void sendToReceivers(IMessage message, Range4D range) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
                if (range.hasPlayerInRange(player)) {
                    sendTo(message, player);
                }
            }
        }
    }
}