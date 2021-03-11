package mekanism.common.base;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.SocketAddress;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.crypto.Cipher;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.client.CEditBookPacket;
import net.minecraft.network.play.client.CEnchantItemPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CJigsawBlockGeneratePacket;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.network.play.client.CLockDifficultyPacket;
import net.minecraft.network.play.client.CMarkRecipeSeenPacket;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPickItemPacket;
import net.minecraft.network.play.client.CPlaceRecipePacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CQueryEntityNBTPacket;
import net.minecraft.network.play.client.CQueryTileEntityNBTPacket;
import net.minecraft.network.play.client.CRenameItemPacket;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.client.CSeenAdvancementsPacket;
import net.minecraft.network.play.client.CSelectTradePacket;
import net.minecraft.network.play.client.CSetDifficultyPacket;
import net.minecraft.network.play.client.CSpectatePacket;
import net.minecraft.network.play.client.CSteerBoatPacket;
import net.minecraft.network.play.client.CTabCompletePacket;
import net.minecraft.network.play.client.CUpdateBeaconPacket;
import net.minecraft.network.play.client.CUpdateCommandBlockPacket;
import net.minecraft.network.play.client.CUpdateJigsawBlockPacket;
import net.minecraft.network.play.client.CUpdateMinecartCommandBlockPacket;
import net.minecraft.network.play.client.CUpdateRecipeBookStatusPacket;
import net.minecraft.network.play.client.CUpdateSignPacket;
import net.minecraft.network.play.client.CUpdateStructureBlockPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket.Flags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;

/**
 * No-op net handler to prevent null pointers in mods that try to send packets to our fake player
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MekFakeNetHandler extends ServerPlayNetHandler {

    public MekFakeNetHandler(MinecraftServer server, MekFakePlayer player) {
        super(server, new FakeNetworkManager(PacketDirection.CLIENTBOUND), player);
    }

    @Override
    public void tick() {
    }

    @Override
    public void resetPosition() {
    }

    @Override
    public void disconnect(ITextComponent textComponent) {
    }

    @Override
    public void handlePlayerInput(CInputPacket packet) {
    }

    @Override
    public void handleMoveVehicle(CMoveVehiclePacket packet) {
    }

    @Override
    public void handleAcceptTeleportPacket(CConfirmTeleportPacket packet) {
    }

    @Override
    public void handleRecipeBookSeenRecipePacket(CMarkRecipeSeenPacket packet) {
    }

    @Override
    public void handleRecipeBookChangeSettingsPacket(CUpdateRecipeBookStatusPacket packet) {
    }

    @Override
    public void handleSeenAdvancements(CSeenAdvancementsPacket packet) {
    }

    @Override
    public void handleCustomCommandSuggestions(CTabCompletePacket packet) {
    }

    @Override
    public void handleSetCommandBlock(CUpdateCommandBlockPacket packet) {
    }

    @Override
    public void handleSetCommandMinecart(CUpdateMinecartCommandBlockPacket packet) {
    }

    @Override
    public void handlePickItem(CPickItemPacket packet) {
    }

    @Override
    public void handleRenameItem(CRenameItemPacket packet) {
    }

    @Override
    public void handleSetBeaconPacket(CUpdateBeaconPacket packet) {
    }

    @Override
    public void handleSetStructureBlock(CUpdateStructureBlockPacket packet) {
    }

    @Override
    public void handleSetJigsawBlock(CUpdateJigsawBlockPacket packet) {
    }

    @Override
    public void handleJigsawGenerate(CJigsawBlockGeneratePacket packet) {
    }

    @Override
    public void handleSelectTrade(CSelectTradePacket packet) {
    }

    @Override
    public void handleEditBook(CEditBookPacket packet) {
    }

    @Override
    public void handleEntityTagQuery(CQueryEntityNBTPacket packet) {
    }

    @Override
    public void handleBlockEntityTagQuery(CQueryTileEntityNBTPacket packet) {
    }

    @Override
    public void handleMovePlayer(CPlayerPacket packet) {
    }

    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch) {
    }

    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch, Set<Flags> relativeSet) {
    }

    @Override
    public void handlePlayerAction(CPlayerDiggingPacket packet) {
    }

    @Override
    public void handleUseItemOn(CPlayerTryUseItemOnBlockPacket packet) {
    }

    @Override
    public void handleUseItem(CPlayerTryUseItemPacket packet) {
    }

    @Override
    public void handleTeleportToEntityPacket(CSpectatePacket packet) {
    }

    @Override
    public void handleResourcePackResponse(CResourcePackStatusPacket packet) {
    }

    @Override
    public void handlePaddleBoat(CSteerBoatPacket packet) {
    }

    @Override
    public void onDisconnect(ITextComponent reason) {
    }

    @Override
    public void send(IPacket<?> packet) {
    }

    @Override
    public void send(IPacket<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> futureListeners) {
    }

    @Override
    public void handleSetCarriedItem(CHeldItemChangePacket packet) {
    }

    @Override
    public void handleChat(CChatMessagePacket packet) {
    }

    @Override
    public void handleAnimate(CAnimateHandPacket packet) {
    }

    @Override
    public void handlePlayerCommand(CEntityActionPacket packet) {
    }

    @Override
    public void handleInteract(CUseEntityPacket packet) {
    }

    @Override
    public void handleClientCommand(CClientStatusPacket packet) {
    }

    @Override
    public void handleContainerClose(CCloseWindowPacket packet) {
    }

    @Override
    public void handleContainerClick(CClickWindowPacket packet) {
    }

    @Override
    public void handlePlaceRecipe(CPlaceRecipePacket packet) {
    }

    @Override
    public void handleContainerButtonClick(CEnchantItemPacket packet) {
    }

    @Override
    public void handleSetCreativeModeSlot(CCreativeInventoryActionPacket packet) {
    }

    @Override
    public void handleContainerAck(CConfirmTransactionPacket packet) {
    }

    @Override
    public void handleSignUpdate(CUpdateSignPacket packet) {
    }

    @Override
    public void handleKeepAlive(CKeepAlivePacket packet) {
    }

    @Override
    public void handlePlayerAbilities(CPlayerAbilitiesPacket packet) {
    }

    @Override
    public void handleClientInformation(CClientSettingsPacket packet) {
    }

    @Override
    public void handleCustomPayload(CCustomPayloadPacket packet) {
    }

    @Override
    public void handleChangeDifficulty(CSetDifficultyPacket packet) {
    }

    @Override
    public void handleLockDifficulty(CLockDifficultyPacket packet) {
    }

    private static class FakeNetworkManager extends NetworkManager {

        public FakeNetworkManager(PacketDirection packetDirection) {
            super(packetDirection);
        }

        @Override
        public void channelActive(ChannelHandlerContext channelHandlerContext) {
        }

        @Override
        public void setProtocol(ProtocolType newState) {
        }

        @Override
        public void channelInactive(ChannelHandlerContext channelHandlerContext) {
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
        }

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, IPacket<?> packet) {
        }

        @Override
        public void send(IPacket<?> packet) {
        }

        @Override
        public void send(IPacket<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener) {
        }

        @Override
        public void tick() {
        }

        @Override
        protected void tickSecond() {
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public void disconnect(ITextComponent message) {
        }

        @Override
        public boolean isMemoryConnection() {
            return false;
        }

        @Override
        public void setEncryptionKey(Cipher splitter, Cipher prepender) {
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public boolean isConnecting() {
            return true;
        }

        @Override
        public void setReadOnly() {
        }

        @Override
        public void setupCompression(int threshold) {
        }

        @Override
        public void handleDisconnection() {
        }

        @Override
        public Channel channel() {
            return null;
        }
    }
}