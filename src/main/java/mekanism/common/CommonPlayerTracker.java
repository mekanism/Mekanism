package mekanism.common;

import mekanism.common.block.BlockCardboardBox;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.capability.DefaultRadiationEntity;
import mekanism.common.network.PacketClearRecipeCache;
import mekanism.common.network.PacketPlayerData;
import mekanism.common.network.PacketRadiationData;
import mekanism.common.network.PacketResetPlayerClient;
import mekanism.common.network.PacketSecurityUpdate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
    public static boolean monitoringCardboardBox;

    public CommonPlayerTracker() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoginEvent(PlayerLoggedInEvent event) {
        if (!event.getPlayer().world.isRemote) {
            Mekanism.packetHandler.sendTo(new PacketSecurityUpdate(), (ServerPlayerEntity) event.getPlayer());
            Mekanism.packetHandler.sendTo(new PacketClearRecipeCache(), (ServerPlayerEntity) event.getPlayer());
            event.getPlayer().getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> PacketRadiationData.sync((ServerPlayerEntity) event.getPlayer()));
        }
    }

    @SubscribeEvent
    public void onPlayerLogoutEvent(PlayerLoggedOutEvent event) {
        PlayerEntity player = event.getPlayer();
        Mekanism.playerState.clearPlayer(player.getUniqueID(), false);
        Mekanism.playerState.clearPlayerServerSideOnly(player.getUniqueID());
    }

    @SubscribeEvent
    public void onPlayerDimChangedEvent(PlayerChangedDimensionEvent event) {
        PlayerEntity player = event.getPlayer();
        Mekanism.playerState.clearPlayer(player.getUniqueID(), false);
        Mekanism.playerState.reapplyServerSideOnly(player);
        player.getCapability(Capabilities.RADIATION_ENTITY_CAPABILITY).ifPresent(c -> PacketRadiationData.sync((ServerPlayerEntity) player));

    }

    @SubscribeEvent
    public void onPlayerStartTrackingEvent(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof PlayerEntity && event.getPlayer() instanceof ServerPlayerEntity) {
            Mekanism.packetHandler.sendTo(new PacketPlayerData(event.getTarget().getUniqueID()), (ServerPlayerEntity) event.getPlayer());
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
        Mekanism.packetHandler.sendToAll(new PacketResetPlayerClient(event.getPlayer().getUniqueID()));
    }

    /**
     * If the player is sneaking and the dest block is a cardboard box, ensure onBlockActivated is called, and that the item use is not.
     *
     * @param blockEvent event
     */
    @SubscribeEvent
    public void rightClickEvent(RightClickBlock blockEvent) {
        if (blockEvent.getPlayer().isSneaking() && blockEvent.getWorld().getBlockState(blockEvent.getPos()).getBlock() instanceof BlockCardboardBox) {
            blockEvent.setUseBlock(Event.Result.ALLOW);
            blockEvent.setUseItem(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        //TODO: What is the point of this/should we check if it is close to the location things are happening?
        // This is just how it used to be in 1.12
        if (event.getEntity() instanceof ItemEntity && monitoringCardboardBox) {
            event.setCanceled(true);
        }
    }
}