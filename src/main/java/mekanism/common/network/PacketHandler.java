package mekanism.common.network;

import mekanism.client.render.hud.MekanismStatusOverlay;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.Version;
import mekanism.common.network.to_client.PacketHitBlockEffect;
import mekanism.common.network.to_client.PacketLightningRender;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.network.to_client.PacketSetDeltaMovement;
import mekanism.common.network.to_client.PacketUpdateTile;
import mekanism.common.network.to_client.configuration.SyncAllSecurityData;
import mekanism.common.network.to_client.container.PacketUpdateContainer;
import mekanism.common.network.to_client.player_data.PacketPlayerData;
import mekanism.common.network.to_client.player_data.PacketResetPlayerClient;
import mekanism.common.network.to_client.qio.PacketUpdateItemViewer;
import mekanism.common.network.to_client.radiation.PacketEnvironmentalRadiationData;
import mekanism.common.network.to_client.radiation.PacketPlayerRadiationData;
import mekanism.common.network.to_client.security.PacketBatchSecurityUpdate;
import mekanism.common.network.to_client.security.PacketSyncSecurity;
import mekanism.common.network.to_client.transmitter.PacketChemicalNetworkContents;
import mekanism.common.network.to_client.transmitter.PacketFluidNetworkContents;
import mekanism.common.network.to_client.transmitter.PacketNetworkScale;
import mekanism.common.network.to_client.transmitter.PacketTransporterBatch;
import mekanism.common.network.to_client.transmitter.PacketTransporterSync;
import mekanism.common.network.to_server.PacketAddTrusted;
import mekanism.common.network.to_server.PacketDropperUse;
import mekanism.common.network.to_server.PacketGearStateUpdate;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiSetEnergy;
import mekanism.common.network.to_server.PacketItemGuiInteract;
import mekanism.common.network.to_server.PacketKey;
import mekanism.common.network.to_server.PacketModeChange;
import mekanism.common.network.to_server.PacketModeChangeCurios;
import mekanism.common.network.to_server.PacketOpenGui;
import mekanism.common.network.to_server.PacketPortableTeleporterTeleport;
import mekanism.common.network.to_server.PacketRadialModeChange;
import mekanism.common.network.to_server.PacketRemoveModule;
import mekanism.common.network.to_server.PacketUpdateModuleSettings;
import mekanism.common.network.to_server.PacketWindowSelect;
import mekanism.common.network.to_server.button.PacketEntityButtonPress;
import mekanism.common.network.to_server.button.PacketItemButtonPress;
import mekanism.common.network.to_server.button.PacketTileButtonPress;
import mekanism.common.network.to_server.configuration_update.PacketBatchConfiguration;
import mekanism.common.network.to_server.configuration_update.PacketEjectColor;
import mekanism.common.network.to_server.configuration_update.PacketEjectConfiguration;
import mekanism.common.network.to_server.configuration_update.PacketInputColor;
import mekanism.common.network.to_server.configuration_update.PacketSideData;
import mekanism.common.network.to_server.filter.PacketEditFilter;
import mekanism.common.network.to_server.filter.PacketNewFilter;
import mekanism.common.network.to_server.frequency.PacketSetFrequencyColor;
import mekanism.common.network.to_server.frequency.PacketSetItemFrequency;
import mekanism.common.network.to_server.frequency.PacketSetTileFrequency;
import mekanism.common.network.to_server.qio.PacketQIOClearCraftingWindow;
import mekanism.common.network.to_server.qio.PacketQIOFillCraftingWindow;
import mekanism.common.network.to_server.qio.PacketQIOItemViewerSlotPlace;
import mekanism.common.network.to_server.qio.PacketQIOItemViewerSlotShiftTake;
import mekanism.common.network.to_server.qio.PacketQIOItemViewerSlotTake;
import mekanism.common.network.to_server.robit.PacketRobitName;
import mekanism.common.network.to_server.robit.PacketRobitSkin;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;

public class PacketHandler extends BasePacketHandler {

    //Client to server instanced packets

    //Server to client instanced packets
    private SimplePacketPayLoad showModeChange;
    private SimplePacketPayLoad killItemViewer;

    public PacketHandler(IEventBus modEventBus, Version version) {
        super(modEventBus, version);
        modEventBus.addListener(RegisterConfigurationTasksEvent.class, event -> {
            ServerConfigurationPacketListener listener = event.getListener();
            event.register(new SyncAllSecurityData(listener));
        });
    }

    public void showModeChange(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, showModeChange);
    }

    public void killItemViewer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, killItemViewer);
    }

    @Override
    protected void registerClientToServer(PacketRegistrar registrar) {
        registrar.play(PacketAddTrusted.TYPE, PacketAddTrusted.STREAM_CODEC);
        registrar.play(PacketDropperUse.TYPE, PacketDropperUse.STREAM_CODEC);
        registrar.play(PacketEditFilter.TYPE, PacketEditFilter.STREAM_CODEC);
        registrar.play(PacketGearStateUpdate.TYPE, PacketGearStateUpdate.STREAM_CODEC);
        registrar.play(PacketGuiInteract.TYPE, PacketGuiInteract.STREAM_CODEC);
        registrar.play(PacketItemGuiInteract.TYPE, PacketItemGuiInteract.STREAM_CODEC);
        registrar.play(PacketGuiSetEnergy.TYPE, PacketGuiSetEnergy.STREAM_CODEC);
        registrar.play(PacketKey.TYPE, PacketKey.STREAM_CODEC);
        registrar.play(PacketModeChange.TYPE, PacketModeChange.STREAM_CODEC);
        registrar.play(PacketModeChangeCurios.TYPE, PacketModeChangeCurios.STREAM_CODEC);
        registrar.play(PacketNewFilter.TYPE, PacketNewFilter.STREAM_CODEC);
        registrar.play(PacketOpenGui.TYPE, PacketOpenGui.STREAM_CODEC);
        registrar.play(PacketPortableTeleporterTeleport.TYPE, PacketPortableTeleporterTeleport.STREAM_CODEC);
        registrar.play(PacketRadialModeChange.TYPE, PacketRadialModeChange.STREAM_CODEC);
        registrar.play(PacketRemoveModule.TYPE, PacketRemoveModule.STREAM_CODEC);
        registrar.play(PacketUpdateModuleSettings.TYPE, PacketUpdateModuleSettings.STREAM_CODEC);
        registrar.play(PacketWindowSelect.TYPE, PacketWindowSelect.STREAM_CODEC);

        //Button Press
        registrar.play(PacketEntityButtonPress.TYPE, PacketEntityButtonPress.STREAM_CODEC);
        registrar.play(PacketItemButtonPress.TYPE, PacketItemButtonPress.STREAM_CODEC);
        registrar.play(PacketTileButtonPress.TYPE, PacketTileButtonPress.STREAM_CODEC);

        //Frequency
        registrar.play(PacketSetItemFrequency.TYPE, PacketSetItemFrequency.STREAM_CODEC);
        registrar.play(PacketSetTileFrequency.TYPE, PacketSetTileFrequency.STREAM_CODEC);
        registrar.play(PacketSetFrequencyColor.TYPE, PacketSetFrequencyColor.STREAM_CODEC);

        //Robit
        registrar.play(PacketRobitName.TYPE, PacketRobitName.STREAM_CODEC);
        registrar.play(PacketRobitSkin.TYPE, PacketRobitSkin.STREAM_CODEC);

        //QIO
        registrar.play(PacketQIOClearCraftingWindow.TYPE, PacketQIOClearCraftingWindow.STREAM_CODEC);
        registrar.play(PacketQIOFillCraftingWindow.TYPE, PacketQIOFillCraftingWindow.STREAM_CODEC);
        registrar.play(PacketQIOItemViewerSlotPlace.TYPE, PacketQIOItemViewerSlotPlace.STREAM_CODEC);
        registrar.play(PacketQIOItemViewerSlotTake.TYPE, PacketQIOItemViewerSlotTake.STREAM_CODEC);
        registrar.play(PacketQIOItemViewerSlotShiftTake.TYPE, PacketQIOItemViewerSlotShiftTake.STREAM_CODEC);

        //Configuration update packets
        registrar.play(PacketBatchConfiguration.TYPE, PacketBatchConfiguration.STREAM_CODEC);
        registrar.play(PacketEjectColor.TYPE, PacketEjectColor.STREAM_CODEC);
        registrar.play(PacketEjectConfiguration.TYPE, PacketEjectConfiguration.STREAM_CODEC);
        registrar.play(PacketInputColor.TYPE, PacketInputColor.STREAM_CODEC);
        registrar.play(PacketSideData.TYPE, PacketSideData.STREAM_CODEC);
    }

    @Override
    protected void registerServerToClient(PacketRegistrar registrar) {
        //Configuration packets
        registrar.configuration(PacketBatchSecurityUpdate.TYPE, PacketBatchSecurityUpdate.STREAM_CODEC);

        //Play packets
        registrar.play(PacketHitBlockEffect.TYPE, PacketHitBlockEffect.STREAM_CODEC);
        registrar.play(PacketLightningRender.TYPE, PacketLightningRender.STREAM_CODEC);
        registrar.play(PacketPlayerData.TYPE, PacketPlayerData.STREAM_CODEC);
        registrar.play(PacketPortalFX.TYPE, PacketPortalFX.STREAM_CODEC);
        registrar.play(PacketEnvironmentalRadiationData.TYPE, PacketEnvironmentalRadiationData.STREAM_CODEC);
        registrar.play(PacketPlayerRadiationData.TYPE, PacketPlayerRadiationData.STREAM_CODEC);
        registrar.play(PacketResetPlayerClient.TYPE, PacketResetPlayerClient.STREAM_CODEC);
        registrar.play(PacketSyncSecurity.TYPE, PacketSyncSecurity.STREAM_CODEC);
        showModeChange = registrar.playInstanced(Mekanism.rl("show_mode_change"), (ignored, context) -> MekanismStatusOverlay.INSTANCE.setTimer());
        registrar.play(PacketUpdateContainer.TYPE, PacketUpdateContainer.STREAM_CODEC);
        registrar.play(PacketUpdateTile.TYPE, PacketUpdateTile.STREAM_CODEC);
        registrar.play(PacketSetDeltaMovement.TYPE, PacketSetDeltaMovement.STREAM_CODEC);

        //QIO
        registrar.play(PacketUpdateItemViewer.TYPE, PacketUpdateItemViewer.STREAM_CODEC);
        killItemViewer = registrar.playInstanced(Mekanism.rl("kill_qio"), (ignored, context) -> {
            if (context.player().containerMenu instanceof QIOItemViewerContainer container) {
                container.handleKill();
            }
        });

        //Transmitters
        registrar.play(PacketNetworkScale.TYPE, PacketNetworkScale.STREAM_CODEC);
        registrar.play(PacketChemicalNetworkContents.TYPE, PacketChemicalNetworkContents.STREAM_CODEC);
        registrar.play(PacketFluidNetworkContents.TYPE, PacketFluidNetworkContents.STREAM_CODEC);
        registrar.play(PacketTransporterBatch.TYPE, PacketTransporterBatch.STREAM_CODEC);
        registrar.play(PacketTransporterSync.TYPE, PacketTransporterSync.STREAM_CODEC);
    }
}