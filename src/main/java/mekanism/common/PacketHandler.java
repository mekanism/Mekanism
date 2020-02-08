package mekanism.common;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.Range4D;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfusionStack;
import mekanism.common.base.ITileNetwork;
import mekanism.common.config.MekanismConfig;
import mekanism.common.network.PacketClearRecipeCache;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketContainerEditMode;
import mekanism.common.network.PacketDataRequest;
import mekanism.common.network.PacketDropperUse;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketEntityMove;
import mekanism.common.network.PacketFlamethrowerData;
import mekanism.common.network.PacketFreeRunnerData;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketItemStack;
import mekanism.common.network.PacketJetpackData;
import mekanism.common.network.PacketKey;
import mekanism.common.network.PacketMekanismTags;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.network.PacketPortableTeleporter;
import mekanism.common.network.PacketPortalFX;
import mekanism.common.network.PacketRedstoneControl;
import mekanism.common.network.PacketRemoveUpgrade;
import mekanism.common.network.PacketRobit;
import mekanism.common.network.PacketScubaTankData;
import mekanism.common.network.PacketSecurityMode;
import mekanism.common.network.PacketSecurityUpdate;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * Mekanism packet handler. As always, use packets sparingly!
 *
 * @author AidanBrady
 */
public class PacketHandler {

    private static final SimpleChannel netHandler = NetworkRegistry.ChannelBuilder.named(Mekanism.rl(Mekanism.MODID))
          .clientAcceptedVersions(getProtocolVersion()::equals)
          .serverAcceptedVersions(getProtocolVersion()::equals)
          .networkProtocolVersion(PacketHandler::getProtocolVersion)
          .simpleChannel();
    private int index = 0;

    private static String getProtocolVersion() {
        return Mekanism.instance == null ? "999.999.999" : Mekanism.instance.versionNumber.toString();
    }

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
            } else if (data instanceof FluidStack) {
                output.writeFluidStack((FluidStack) data);
            } else if (data instanceof ChemicalStack) {
                writeChemicalStack(output, (ChemicalStack<?>) data);
            } else if (data instanceof CompoundNBT) {
                output.writeCompoundTag((CompoundNBT) data);
            } else if (data instanceof ResourceLocation) {
                output.writeResourceLocation((ResourceLocation) data);
            } else if (data instanceof Enum) {
                output.writeEnumValue((Enum<?>) data);
            } else if (data instanceof int[]) {
                for (int i : (int[]) data) {
                    output.writeInt(i);
                }
            } else if (data instanceof byte[]) {
                for (byte b : (byte[]) data) {
                    output.writeByte(b);
                }
            } else if (data instanceof List) {
                encode(((List<?>) data).toArray(), output);
            } else {
                throw new RuntimeException("Un-encodable data passed to encode(): " + data + ", full data: " + Arrays.toString(dataValues));
            }
        }
    }

    //TODO: Move some of this chemical stuff to a util thing in API
    public static void writeChemicalStack(PacketBuffer buffer, ChemicalStack<?> stack) {
        if (stack.isEmpty()) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            stack.writeToPacket(buffer);
        }
    }

    public static GasStack readGasStack(PacketBuffer buffer) {
        return buffer.readBoolean() ? GasStack.readFromPacket(buffer) : GasStack.EMPTY;
    }

    public static InfusionStack readInfusionStack(PacketBuffer buffer) {
        return buffer.readBoolean() ? InfusionStack.readFromPacket(buffer) : InfusionStack.EMPTY;
    }

    public static String readString(PacketBuffer buffer) {
        //TODO: Re-evaluate, this method is currently used because buffer.readString() is clientside only, so it mimics its behaviour so that servers don't crash
        return buffer.readString(Short.MAX_VALUE);
    }

    public static void log(String log) {
        if (MekanismConfig.general.logPackets.get()) {
            Mekanism.logger.info(log);
        }
    }

    public static PlayerEntity getPlayer(Supplier<Context> context) {
        return Mekanism.proxy.getPlayer(context);
    }

    //TODO: Make sure to not set packets as handled if we are receiving it from the wrong side
    public void initialize() {
        registerMessage(PacketRobit.class, PacketRobit::encode, PacketRobit::decode, PacketRobit::handle);
        registerMessage(PacketTransmitterUpdate.class, PacketTransmitterUpdate::encode, PacketTransmitterUpdate::decode, PacketTransmitterUpdate::handle);
        registerMessage(PacketItemStack.class, PacketItemStack::encode, PacketItemStack::decode, PacketItemStack::handle);
        registerMessage(PacketTileEntity.class, PacketTileEntity::encode, PacketTileEntity::decode, PacketTileEntity::handle);
        registerMessage(PacketPortalFX.class, PacketPortalFX::encode, PacketPortalFX::decode, PacketPortalFX::handle);
        registerMessage(PacketDataRequest.class, PacketDataRequest::encode, PacketDataRequest::decode, PacketDataRequest::handle);
        registerMessage(PacketSecurityMode.class, PacketSecurityMode::encode, PacketSecurityMode::decode, PacketSecurityMode::handle);
        registerMessage(PacketPortableTeleporter.class, PacketPortableTeleporter::encode, PacketPortableTeleporter::decode, PacketPortableTeleporter::handle);
        registerMessage(PacketRemoveUpgrade.class, PacketRemoveUpgrade::encode, PacketRemoveUpgrade::decode, PacketRemoveUpgrade::handle);
        registerMessage(PacketRedstoneControl.class, PacketRedstoneControl::encode, PacketRedstoneControl::decode, PacketRedstoneControl::handle);
        registerMessage(PacketNewFilter.class, PacketNewFilter::encode, PacketNewFilter::decode, PacketNewFilter::handle);
        registerMessage(PacketEditFilter.class, PacketEditFilter::encode, PacketEditFilter::decode, PacketEditFilter::handle);
        registerMessage(PacketConfigurationUpdate.class, PacketConfigurationUpdate::encode, PacketConfigurationUpdate::decode, PacketConfigurationUpdate::handle);
        registerMessage(PacketJetpackData.class, PacketJetpackData::encode, PacketJetpackData::decode, PacketJetpackData::handle);
        registerMessage(PacketKey.class, PacketKey::encode, PacketKey::decode, PacketKey::handle);
        registerMessage(PacketScubaTankData.class, PacketScubaTankData::encode, PacketScubaTankData::decode, PacketScubaTankData::handle);
        registerMessage(PacketContainerEditMode.class, PacketContainerEditMode::encode, PacketContainerEditMode::decode, PacketContainerEditMode::handle);
        registerMessage(PacketFlamethrowerData.class, PacketFlamethrowerData::encode, PacketFlamethrowerData::decode, PacketFlamethrowerData::handle);
        registerMessage(PacketDropperUse.class, PacketDropperUse::encode, PacketDropperUse::decode, PacketDropperUse::handle);
        registerMessage(PacketEntityMove.class, PacketEntityMove::encode, PacketEntityMove::decode, PacketEntityMove::handle);
        registerMessage(PacketSecurityUpdate.class, PacketSecurityUpdate::encode, PacketSecurityUpdate::decode, PacketSecurityUpdate::handle);
        registerMessage(PacketFreeRunnerData.class, PacketFreeRunnerData::encode, PacketFreeRunnerData::decode, PacketFreeRunnerData::handle);
        registerMessage(PacketGuiButtonPress.class, PacketGuiButtonPress::encode, PacketGuiButtonPress::decode, PacketGuiButtonPress::handle);

        registerMessage(PacketMekanismTags.class, PacketMekanismTags::encode, PacketMekanismTags::decode, PacketMekanismTags::handle);
        registerMessage(PacketClearRecipeCache.class, PacketClearRecipeCache::encode, PacketClearRecipeCache::decode, PacketClearRecipeCache::handle);
    }

    private <MSG> void registerMessage(Class<MSG> type, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<Context>> consumer) {
        registerMessage(index++, type, encoder, decoder, consumer);
    }

    //TODO: Figure out a better way to do this, for now with generators we are just starting it at id 100 to make sure they don't clash
    // Given we will rewrite our packet system at some point, I am not bothering to do more than just a patch for now
    // One better solution may be to register the information to Mekanism, from the module and let it add it when it is adding the other ones
    public <MSG> void registerMessage(int id, Class<MSG> type, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<Context>> consumer) {
        netHandler.registerMessage(id, type, encoder, decoder, consumer);
    }

    /**
     * Send this message to the specified player.
     *
     * @param message - the message to send
     * @param player  - the player to send it to
     */
    public <MSG> void sendTo(MSG message, ServerPlayerEntity player) {
        netHandler.sendTo(message, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

    /**
     * Send this message to everyone connected to the server.
     *
     * @param message - message to send
     */
    public <MSG> void sendToAll(MSG message) {
        netHandler.send(PacketDistributor.ALL.noArg(), message);
    }

    /**
     * Send this message to everyone within the supplied dimension.
     *
     * @param message   - the message to send
     * @param dimension - the dimension to target
     */
    public <MSG> void sendToDimension(MSG message, DimensionType dimension) {
        netHandler.send(PacketDistributor.DIMENSION.with(() -> dimension), message);
    }

    /**
     * Send this message to the server.
     *
     * @param message - the message to send
     */
    public <MSG> void sendToServer(MSG message) {
        netHandler.sendToServer(message);
    }

    public <TILE extends TileEntity & ITileNetwork> void sendUpdatePacket(TILE tile) {
        sendToAllTracking(new PacketTileEntity(tile), tile);
    }

    public <MSG> void sendToAllTracking(MSG message, TileEntity tile) {
        sendToAllTracking(message, tile.getWorld(), tile.getPos());
    }

    public <MSG> void sendToAllTracking(MSG message, World world, BlockPos pos) {
        if (world instanceof ServerWorld) {
            //If we have a ServerWorld just directly figure out the ChunkPos so as to not require looking up the chunk
            // This provides a decent performance boost over using the packet distributor
            ((ServerWorld) world).getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(pos), false).forEach(p -> sendTo(message, p));
        } else {
            //Otherwise fallback to entities tracking the chunk if some mod did something odd and our world is not a ServerWorld
            netHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunk(pos.getX() >> 4, pos.getZ() >> 4)), message);
        }
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