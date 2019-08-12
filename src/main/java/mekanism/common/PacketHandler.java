package mekanism.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;
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
import mekanism.common.network.PacketDropperUse;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketEntityMove;
import mekanism.common.network.PacketFlamethrowerData;
import mekanism.common.network.PacketFreeRunnerData;
import mekanism.common.network.PacketItemStack;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketKey;
import mekanism.common.network.PacketLogisticalSorterGui;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.network.PacketOredictionificatorGui;
import mekanism.common.network.PacketPortableTeleporter;
import mekanism.common.network.PacketPortalFX;
import mekanism.common.network.PacketRedstoneControl;
import mekanism.common.network.PacketRemoveUpgrade;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketScubaTankData;
import mekanism.common.network.PacketSecurityMode;
import mekanism.common.network.PacketSecurityUpdate;
import mekanism.common.network.PacketSimpleGui;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.network.PacketTransmitterUpdate;
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
import net.minecraft.world.dimension.DimensionType;
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
    private static final SimpleChannel netHandler = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Mekanism.MODID, "main_channel"))
          .clientAcceptedVersions(PROTOCOL_VERSION::equals).serverAcceptedVersions(PROTOCOL_VERSION::equals).networkProtocolVersion(() -> PROTOCOL_VERSION).simpleChannel();

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
                output.writeString((String) data);
            } else if (data instanceof UUID) {
                output.writeUniqueId((UUID) data);
            } else if (data instanceof Direction) {
                output.writeInt(((Direction) data).ordinal());
            } else if (data instanceof ItemStack) {
                output.writeItemStack((ItemStack) data);
            } else if (data instanceof CompoundNBT) {
                output.writeCompoundTag((CompoundNBT) data);
            } else if (data instanceof ResourceLocation) {
                output.writeResourceLocation((ResourceLocation) data);
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

        netHandler.registerMessage(disc++, PacketRobit.class, PacketRobit::encode, PacketRobit::decode, PacketRobit::handle);
        netHandler.registerMessage(disc++, PacketTransmitterUpdate.class, PacketTransmitterUpdate::encode, PacketTransmitterUpdate::decode, PacketTransmitterUpdate::handle);
        netHandler.registerMessage(disc++, PacketItemStack.class, PacketItemStack::encode, PacketItemStack::decode, PacketItemStack::handle);
        netHandler.registerMessage(disc++, PacketTileEntity.class, PacketTileEntity::encode, PacketTileEntity::decode, PacketTileEntity::handle);
        netHandler.registerMessage(disc++, PacketPortalFX.class, PacketPortalFX::encode, PacketPortalFX::decode, PacketPortalFX::handle);
        netHandler.registerMessage(disc++, PacketDataRequest.class, PacketDataRequest::encode, PacketDataRequest::decode, PacketDataRequest::handle);
        netHandler.registerMessage(disc++, PacketOredictionificatorGui.class, PacketOredictionificatorGui::encode, PacketOredictionificatorGui::decode, PacketOredictionificatorGui::handle);
        netHandler.registerMessage(disc++, PacketSecurityMode.class, PacketSecurityMode::encode, PacketSecurityMode::decode, PacketSecurityMode::handle);
        netHandler.registerMessage(disc++, PacketPortableTeleporter.class, PacketPortableTeleporter::encode, PacketPortableTeleporter::decode, PacketPortableTeleporter::handle);
        netHandler.registerMessage(disc++, PacketRemoveUpgrade.class, PacketRemoveUpgrade::encode, PacketRemoveUpgrade::decode, PacketRemoveUpgrade::handle);
        netHandler.registerMessage(disc++, PacketRedstoneControl.class, PacketRedstoneControl::encode, PacketRedstoneControl::decode, PacketRedstoneControl::handle);
        netHandler.registerMessage(disc++, PacketLogisticalSorterGui.class, PacketLogisticalSorterGui::encode, PacketLogisticalSorterGui::decode, PacketLogisticalSorterGui::handle);
        netHandler.registerMessage(disc++, PacketNewFilter.class, PacketNewFilter::encode, PacketNewFilter::decode, PacketNewFilter::handle);
        netHandler.registerMessage(disc++, PacketEditFilter.class, PacketEditFilter::encode, PacketEditFilter::decode, PacketEditFilter::handle);
        netHandler.registerMessage(disc++, PacketConfigurationUpdate.class, PacketConfigurationUpdate::encode, PacketConfigurationUpdate::decode, PacketConfigurationUpdate::handle);
        netHandler.registerMessage(disc++, PacketSimpleGui.class, PacketSimpleGui::encode, PacketSimpleGui::decode, PacketSimpleGui::handle);
        netHandler.registerMessage(disc++, PacketDigitalMinerGui.class, PacketDigitalMinerGui::encode, PacketDigitalMinerGui::decode, PacketDigitalMinerGui::handle);
        netHandler.registerMessage(disc++, PacketJetpackData.class, PacketJetpackData::encode, PacketJetpackData::decode, PacketJetpackData::handle);
        netHandler.registerMessage(disc++, PacketKey.class, PacketKey::encode, PacketKey::decode, PacketKey::handle);
        netHandler.registerMessage(disc++, PacketScubaTankData.class, PacketScubaTankData::encode, PacketScubaTankData::decode, PacketScubaTankData::handle);
        netHandler.registerMessage(disc++, PacketConfigSync.class, PacketConfigSync::encode, PacketConfigSync::decode, PacketConfigSync::handle);
        netHandler.registerMessage(disc++, PacketBoxBlacklist.class, PacketBoxBlacklist::encode, PacketBoxBlacklist::decode, PacketBoxBlacklist::handle);
        netHandler.registerMessage(disc++, PacketContainerEditMode.class, PacketContainerEditMode::encode, PacketContainerEditMode::decode, PacketContainerEditMode::handle);
        netHandler.registerMessage(disc++, PacketFlamethrowerData.class, PacketFlamethrowerData::encode, PacketFlamethrowerData::decode, PacketFlamethrowerData::handle);
        netHandler.registerMessage(disc++, PacketDropperUse.class, PacketDropperUse::encode, PacketDropperUse::decode, PacketDropperUse::handle);
        netHandler.registerMessage(disc++, PacketEntityMove.class, PacketEntityMove::encode, PacketEntityMove::decode, PacketEntityMove::handle);
        netHandler.registerMessage(disc++, PacketSecurityUpdate.class, PacketSecurityUpdate::encode, PacketSecurityUpdate::decode, PacketSecurityUpdate::handle);
        netHandler.registerMessage(disc++, PacketFreeRunnerData.class, PacketFreeRunnerData::encode, PacketFreeRunnerData::decode, PacketFreeRunnerData::handle);
    }

    /**
     * Send this message to the specified player.
     *
     * @param message - the message to send
     * @param player  - the player to send it to
     */
    public <MSG> void sendTo(MSG message, ServerPlayerEntity player) {
        netHandler.sendTo(message, player);
    }

    /**
     * Send this message to everyone connected to the server.
     *
     * @param message - message to send
     */
    public <MSG> void sendToAll(MSG message) {
        netHandler.sendToAll(message);
    }

    /**
     * Send this message to everyone within a certain range of a point.
     *
     * @param message - the message to send
     * @param point   - the TargetPoint around which to send
     */
    public <MSG> void sendToAllAround(MSG message, TargetPoint point) {
        netHandler.sendToAllAround(message, point);
    }

    /**
     * Send this message to everyone within the supplied dimension.
     *
     * @param message     - the message to send
     * @param dimensionId - the dimension id to target
     */
    public <MSG> void sendToDimension(MSG message, DimensionType dimension) {
        netHandler.sendToDimension(message, dimension);
    }

    /**
     * Send this message to the server.
     *
     * @param message - the message to send
     */
    public <MSG> void sendToServer(MSG message) {
        netHandler.sendToServer(message);
    }

    /**
     * Send this message to all players within a defined AABB cuboid.
     *
     * @param message - the message to send
     * @param cuboid  - the AABB cuboid to send the packet in
     * @param dimension   - the dimension the cuboid is in
     */
    public <MSG> void sendToCuboid(MSG message, AxisAlignedBB cuboid, DimensionType dimension) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && cuboid != null) {
            for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
                if (player.dimension.equals(dimension) && cuboid.contains(new Vec3d(player.posX, player.posY, player.posZ))) {
                    sendTo(message, player);
                }
            }
        }
    }

    public <TILE extends TileEntity & ITileNetwork> void sendUpdatePacket(TILE tile) {
        sendToAllTracking(new PacketTileEntity(tile), tile);
    }

    public <MSG> void sendToAllTracking(MSG message, TileEntity tile) {
        BlockPos pos = tile.getPos();
        sendToAllTracking(message, tile.getWorld().getDimension().getType(), pos.getX(), pos.getY(), pos.getZ());
    }

    public <MSG> void sendToAllTracking(MSG message, Coord4D point) {
        sendToAllTracking(message, point.dimension, point.x, point.y, point.z);
    }

    public <MSG> void sendToAllTracking(MSG message, DimensionType dimension, double x, double y, double z) {
        //Range is ignored for sendToAllTracking, and only gets sent to clients that have the location loaded
        sendToAllTracking(message, new TargetPoint(x, y, z, 1, dimension));
    }

    public <MSG> void sendToAllTracking(MSG message, TargetPoint point) {
        netHandler.sendToAllTracking(message, point);
    }

    //TODO: change Network stuff over to using this
    public <MSG> void sendToReceivers(MSG message, Range4D range) {
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