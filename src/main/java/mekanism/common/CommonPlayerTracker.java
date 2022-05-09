package mekanism.common;

import mekanism.common.block.BlockCardboardBox;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.radiation.capability.DefaultRadiationEntity;
import mekanism.common.network.to_client.PacketPlayerData;
import mekanism.common.network.to_client.PacketRadiationData;
import mekanism.common.network.to_client.PacketResetPlayerClient;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommonPlayerTracker {

    /*private static final Component ALPHA_WARNING = MekanismLang.LOG_FORMAT.translateColored(EnumColor.RED, MekanismLang.MEKANISM, EnumColor.GRAY,
          MekanismLang.ALPHA_WARNING.translate(EnumColor.INDIGO, ChatFormatting.UNDERLINE, new ClickEvent(Action.OPEN_URL,
                "https://github.com/mekanism/Mekanism#alpha-status"), MekanismLang.ALPHA_WARNING_HERE));*/

    public CommonPlayerTracker() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoginEvent(PlayerLoggedInEvent event) {
        Player player = event.getPlayer();
        if (!player.level.isClientSide) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            Mekanism.packetHandler().sendTo(new PacketSecurityUpdate(), serverPlayer);
            player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> PacketRadiationData.sync(serverPlayer));
            //player.sendMessage(ALPHA_WARNING, Util.NIL_UUID);
        }
    }

    @SubscribeEvent
    public void onPlayerLogoutEvent(PlayerLoggedOutEvent event) {
        Player player = event.getPlayer();
        Mekanism.playerState.clearPlayer(player.getUUID(), false);
        Mekanism.playerState.clearPlayerServerSideOnly(player.getUUID());
    }

    @SubscribeEvent
    public void onPlayerDimChangedEvent(PlayerChangedDimensionEvent event) {
        ServerPlayer player = (ServerPlayer) event.getPlayer();
        Mekanism.playerState.clearPlayer(player.getUUID(), false);
        Mekanism.playerState.reapplyServerSideOnly(player);
        player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> PacketRadiationData.sync(player));
        RadiationManager.INSTANCE.updateClientRadiation(player);
    }

    @SubscribeEvent
    public void onPlayerStartTrackingEvent(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player player && event.getPlayer() instanceof ServerPlayer serverPlayer) {
            Mekanism.packetHandler().sendTo(new PacketPlayerData(player.getUUID()), serverPlayer);
        }
    }

    @SubscribeEvent
    public void attachCaps(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            DefaultRadiationEntity.Provider radiationProvider = new DefaultRadiationEntity.Provider();
            event.addCapability(DefaultRadiationEntity.Provider.NAME, radiationProvider);
            event.addListener(radiationProvider::invalidate);
        }
    }

    @SubscribeEvent
    public void cloneEvent(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(cap ->
              event.getPlayer().getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> c.deserializeNBT(cap.serializeNBT())));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public void respawnEvent(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayer player = (ServerPlayer) event.getPlayer();
        player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> {
            if (!event.isEndConquered()) {
                //If the player is returning from the end don't reset radiation
                c.set(0);
            }
            PacketRadiationData.sync(player);
        });
        RadiationManager.INSTANCE.updateClientRadiation(player);
        Mekanism.packetHandler().sendToAll(new PacketResetPlayerClient(player.getUUID()));
    }

    /**
     * If the player is sneaking and the dest block is a cardboard box, ensure onBlockActivated is called, and that the item use is not.
     *
     * @param blockEvent event
     */
    @SubscribeEvent
    public void rightClickEvent(RightClickBlock blockEvent) {
        if (blockEvent.getPlayer().isShiftKeyDown() && blockEvent.getWorld().getBlockState(blockEvent.getPos()).getBlock() instanceof BlockCardboardBox) {
            blockEvent.setUseBlock(Event.Result.ALLOW);
            blockEvent.setUseItem(Event.Result.DENY);
        }
    }
}