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
    public void captureCurrentPosition() {
    }

    @Override
    public void disconnect(ITextComponent textComponent) {
    }

    @Override
    public void processInput(CInputPacket packet) {
    }

    @Override
    public void processVehicleMove(CMoveVehiclePacket packet) {
    }

    @Override
    public void processConfirmTeleport(CConfirmTeleportPacket packet) {
    }

    @Override
    public void handleRecipeBookUpdate(CMarkRecipeSeenPacket packet) {
    }

    @Override
    public void func_241831_a(CUpdateRecipeBookStatusPacket packet) {
    }

    @Override
    public void handleSeenAdvancements(CSeenAdvancementsPacket packet) {
    }

    @Override
    public void processTabComplete(CTabCompletePacket packet) {
    }

    @Override
    public void processUpdateCommandBlock(CUpdateCommandBlockPacket packet) {
    }

    @Override
    public void processUpdateCommandMinecart(CUpdateMinecartCommandBlockPacket packet) {
    }

    @Override
    public void processPickItem(CPickItemPacket packet) {
    }

    @Override
    public void processRenameItem(CRenameItemPacket packet) {
    }

    @Override
    public void processUpdateBeacon(CUpdateBeaconPacket packet) {
    }

    @Override
    public void processUpdateStructureBlock(CUpdateStructureBlockPacket packet) {
    }

    @Override
    public void func_217262_a(CUpdateJigsawBlockPacket packet) {
    }

    @Override
    public void func_230549_a_(CJigsawBlockGeneratePacket packet) {
    }

    @Override
    public void processSelectTrade(CSelectTradePacket packet) {
    }

    @Override
    public void processEditBook(CEditBookPacket packet) {
    }

    @Override
    public void processNBTQueryEntity(CQueryEntityNBTPacket packet) {
    }

    @Override
    public void processNBTQueryBlockEntity(CQueryTileEntityNBTPacket packet) {
    }

    @Override
    public void processPlayer(CPlayerPacket packet) {
    }

    @Override
    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {
    }

    @Override
    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<Flags> relativeSet) {
    }

    @Override
    public void processPlayerDigging(CPlayerDiggingPacket packet) {
    }

    @Override
    public void processTryUseItemOnBlock(CPlayerTryUseItemOnBlockPacket packet) {
    }

    @Override
    public void processTryUseItem(CPlayerTryUseItemPacket packet) {
    }

    @Override
    public void handleSpectate(CSpectatePacket packet) {
    }

    @Override
    public void handleResourcePackStatus(CResourcePackStatusPacket packet) {
    }

    @Override
    public void processSteerBoat(CSteerBoatPacket packet) {
    }

    @Override
    public void onDisconnect(ITextComponent reason) {
    }

    @Override
    public void sendPacket(IPacket<?> packet) {
    }

    @Override
    public void sendPacket(IPacket<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> futureListeners) {
    }

    @Override
    public void processHeldItemChange(CHeldItemChangePacket packet) {
    }

    @Override
    public void processChatMessage(CChatMessagePacket packet) {
    }

    @Override
    public void handleAnimation(CAnimateHandPacket packet) {
    }

    @Override
    public void processEntityAction(CEntityActionPacket packet) {
    }

    @Override
    public void processUseEntity(CUseEntityPacket packet) {
    }

    @Override
    public void processClientStatus(CClientStatusPacket packet) {
    }

    @Override
    public void processCloseWindow(CCloseWindowPacket packet) {
    }

    @Override
    public void processClickWindow(CClickWindowPacket packet) {
    }

    @Override
    public void processPlaceRecipe(CPlaceRecipePacket packet) {
    }

    @Override
    public void processEnchantItem(CEnchantItemPacket packet) {
    }

    @Override
    public void processCreativeInventoryAction(CCreativeInventoryActionPacket packet) {
    }

    @Override
    public void processConfirmTransaction(CConfirmTransactionPacket packet) {
    }

    @Override
    public void processUpdateSign(CUpdateSignPacket packet) {
    }

    @Override
    public void processKeepAlive(CKeepAlivePacket packet) {
    }

    @Override
    public void processPlayerAbilities(CPlayerAbilitiesPacket packet) {
    }

    @Override
    public void processClientSettings(CClientSettingsPacket packet) {
    }

    @Override
    public void processCustomPayload(CCustomPayloadPacket packet) {
    }

    @Override
    public void func_217263_a(CSetDifficultyPacket packet) {
    }

    @Override
    public void func_217261_a(CLockDifficultyPacket packet) {
    }

    private static class FakeNetworkManager extends NetworkManager {

        public FakeNetworkManager(PacketDirection packetDirection) {
            super(packetDirection);
        }

        @Override
        public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
        }

        @Override
        public void setConnectionState(ProtocolType newState) {
        }

        @Override
        public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) {
        }

        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, IPacket<?> packet) throws Exception {
        }

        @Override
        public void sendPacket(IPacket<?> packet) {
        }

        @Override
        public void sendPacket(IPacket<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> p_201058_2_) {
        }

        @Override
        public void tick() {
        }

        @Override
        protected void func_241877_b() {
        }

        @Override
        public SocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public void closeChannel(ITextComponent message) {
        }

        @Override
        public boolean isLocalChannel() {
            return false;
        }

        @Override
        public void func_244777_a(Cipher splitter, Cipher prepender) {
        }

        @Override
        public boolean isChannelOpen() {
            return false;
        }

        @Override
        public boolean hasNoChannel() {
            return true;
        }

        @Override
        public void disableAutoRead() {
        }

        @Override
        public void setCompressionThreshold(int threshold) {
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