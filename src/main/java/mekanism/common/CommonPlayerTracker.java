package mekanism.common;

import mekanism.common.block.BlockCardboardBox;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.VanillaGhostStackSyncFix;
import mekanism.common.lib.radiation.capability.DefaultRadiationEntity;
import mekanism.common.network.to_client.PacketPlayerData;
import mekanism.common.network.to_client.PacketRadiationData;
import mekanism.common.network.to_client.PacketResetPlayerClient;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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

    /*private static final ITextComponent ALPHA_WARNING = MekanismLang.LOG_FORMAT.translateColored(EnumColor.RED, MekanismLang.MEKANISM, EnumColor.GRAY,
          MekanismLang.ALPHA_WARNING.translate(EnumColor.INDIGO, TextFormatting.UNDERLINE, new ClickEvent(Action.OPEN_URL,
                "https://github.com/mekanism/Mekanism#alpha-status"), MekanismLang.ALPHA_WARNING_HERE));*/

    public CommonPlayerTracker() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoginEvent(PlayerLoggedInEvent event) {
        PlayerEntity player = event.getPlayer();
        if (!player.level.isClientSide) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            Mekanism.packetHandler.sendTo(new PacketSecurityUpdate(), serverPlayer);
            player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> PacketRadiationData.sync(serverPlayer));
            player.inventoryMenu.addSlotListener(new VanillaGhostStackSyncFix(serverPlayer));
        }
    }

    @SubscribeEvent
    public void onPlayerLogoutEvent(PlayerLoggedOutEvent event) {
        PlayerEntity player = event.getPlayer();
        Mekanism.playerState.clearPlayer(player.getUUID(), false);
        Mekanism.playerState.clearPlayerServerSideOnly(player.getUUID());
    }

    @SubscribeEvent
    public void onPlayerDimChangedEvent(PlayerChangedDimensionEvent event) {
        PlayerEntity player = event.getPlayer();
        Mekanism.playerState.clearPlayer(player.getUUID(), false);
        Mekanism.playerState.reapplyServerSideOnly(player);
        player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> PacketRadiationData.sync((ServerPlayerEntity) player));

    }

    @SubscribeEvent
    public void onPlayerStartTrackingEvent(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof PlayerEntity && event.getPlayer() instanceof ServerPlayerEntity) {
            Mekanism.packetHandler.sendTo(new PacketPlayerData(event.getTarget().getUUID()), (ServerPlayerEntity) event.getPlayer());
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
        event.getOriginal().getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(cap ->
              event.getPlayer().getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> c.deserializeNBT(cap.serializeNBT())));
    }

    @SubscribeEvent
    public void respawnEvent(PlayerEvent.PlayerRespawnEvent event) {
        event.getPlayer().getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> {
            c.set(0);
            PacketRadiationData.sync((ServerPlayerEntity) event.getPlayer());
        });
        Mekanism.packetHandler.sendToAll(new PacketResetPlayerClient(event.getPlayer().getUUID()));
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