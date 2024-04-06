package mekanism.common.network;

import mekanism.client.render.hud.MekanismStatusOverlay;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
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
import mekanism.common.network.to_client.qio.PacketBatchItemViewerSync;
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
import net.neoforged.neoforge.network.event.OnGameConfigurationEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class PacketHandler extends BasePacketHandler {

    //Client to server instanced packets
    private IMekanismPacket<PlayPayloadContext> requestQIOData;

    //Server to client instanced packets
    private IMekanismPacket<PlayPayloadContext> showModeChange;
    private IMekanismPacket<PlayPayloadContext> killItemViewer;

    public PacketHandler(IEventBus modEventBus, String modid, Version version) {
        super(modEventBus, modid, version);
        modEventBus.addListener(OnGameConfigurationEvent.class, event -> {
            ServerConfigurationPacketListener listener = event.getListener();
            event.register(new SyncAllSecurityData(listener));
        });
    }

    public void requestQIOData() {
        PacketUtils.sendToServer(requestQIOData);
    }

    public void showModeChange(ServerPlayer serverPlayer) {
        PacketUtils.sendTo(showModeChange, serverPlayer);
    }

    public void killItemViewer(ServerPlayer serverPlayer) {
        PacketUtils.sendTo(killItemViewer, serverPlayer);
    }

    @Override
    protected void registerClientToServer(PacketRegistrar registrar) {
        registrar.play(PacketAddTrusted.ID, PacketAddTrusted::new);
        registrar.play(PacketDropperUse.ID, PacketDropperUse::new);
        registrar.play(PacketEditFilter.ID, PacketEditFilter::decode);
        registrar.play(PacketGearStateUpdate.ID, PacketGearStateUpdate::new);
        registrar.play(PacketGuiInteract.ID, PacketGuiInteract::decode);
        registrar.play(PacketItemGuiInteract.ID, PacketItemGuiInteract::new);
        registrar.play(PacketGuiSetEnergy.ID, PacketGuiSetEnergy::new);
        registrar.play(PacketKey.ID, PacketKey::new);
        registrar.play(PacketModeChange.ID, PacketModeChange::new);
        registrar.play(PacketModeChangeCurios.ID, PacketModeChangeCurios::new);
        registrar.play(PacketNewFilter.ID, PacketNewFilter::new);
        registrar.play(PacketOpenGui.ID, PacketOpenGui::new);
        registrar.play(PacketPortableTeleporterTeleport.ID, PacketPortableTeleporterTeleport::new);
        registrar.play(PacketRadialModeChange.ID, PacketRadialModeChange::new);
        registrar.play(PacketRemoveModule.ID, PacketRemoveModule::new);
        registrar.play(PacketUpdateModuleSettings.ID, PacketUpdateModuleSettings::decode);
        registrar.play(PacketWindowSelect.ID, PacketWindowSelect::decode);

        //Button Press
        registrar.play(PacketEntityButtonPress.ID, PacketEntityButtonPress::new);
        registrar.play(PacketItemButtonPress.ID, PacketItemButtonPress::new);
        registrar.play(PacketTileButtonPress.ID, PacketTileButtonPress::new);

        //Frequency
        registrar.play(PacketSetItemFrequency.ID, PacketSetItemFrequency::new);
        registrar.play(PacketSetTileFrequency.ID, PacketSetTileFrequency::new);
        registrar.play(PacketSetFrequencyColor.ID, PacketSetFrequencyColor::new);

        //Robit
        registrar.play(PacketRobitName.ID, PacketRobitName::new);
        registrar.play(PacketRobitSkin.ID, PacketRobitSkin::new);

        //QIO
        registrar.play(PacketQIOClearCraftingWindow.ID, PacketQIOClearCraftingWindow::new);
        registrar.play(PacketQIOFillCraftingWindow.ID, PacketQIOFillCraftingWindow::decode);
        registrar.play(PacketQIOItemViewerSlotPlace.ID, PacketQIOItemViewerSlotPlace::new);
        registrar.play(PacketQIOItemViewerSlotTake.ID, PacketQIOItemViewerSlotTake::new);
        registrar.play(PacketQIOItemViewerSlotShiftTake.ID, PacketQIOItemViewerSlotShiftTake::new);
        requestQIOData = registrar.playInstanced(Mekanism.rl("request_qio_data"), context -> PacketUtils.asServerPlayer(context).ifPresent(player -> {
            if (player.containerMenu instanceof QIOItemViewerContainer container) {
                QIOFrequency freq = container.getFrequency();
                if (freq != null) {
                    freq.openItemViewer(player);
                }
            }
        }));

        //Configuration update packets
        registrar.play(PacketBatchConfiguration.ID, PacketBatchConfiguration::new);
        registrar.play(PacketEjectColor.ID, PacketEjectColor::new);
        registrar.play(PacketEjectConfiguration.ID, PacketEjectConfiguration::new);
        registrar.play(PacketInputColor.ID, PacketInputColor::new);
        registrar.play(PacketSideData.ID, PacketSideData::new);
    }

    @Override
    protected void registerServerToClient(PacketRegistrar registrar) {
        //Configuration packets
        registrar.configuration(PacketBatchSecurityUpdate.ID, PacketBatchSecurityUpdate::new);

        //Play packets
        registrar.play(PacketHitBlockEffect.ID, PacketHitBlockEffect::new);
        registrar.play(PacketLightningRender.ID, PacketLightningRender::new);
        registrar.play(PacketPlayerData.ID, PacketPlayerData::new);
        registrar.play(PacketPortalFX.ID, PacketPortalFX::new);
        registrar.play(PacketEnvironmentalRadiationData.ID, PacketEnvironmentalRadiationData::new);
        registrar.play(PacketPlayerRadiationData.ID, PacketPlayerRadiationData::new);
        registrar.play(PacketResetPlayerClient.ID, PacketResetPlayerClient::new);
        registrar.play(PacketSyncSecurity.ID, PacketSyncSecurity::new);
        showModeChange = registrar.playInstanced(Mekanism.rl("show_mode_change"), context -> MekanismStatusOverlay.INSTANCE.setTimer());
        registrar.play(PacketUpdateContainer.ID, PacketUpdateContainer::new);
        registrar.play(PacketUpdateTile.ID, PacketUpdateTile::new);
        registrar.play(PacketSetDeltaMovement.ID, PacketSetDeltaMovement::new);

        //QIO
        registrar.play(PacketBatchItemViewerSync.ID, PacketBatchItemViewerSync::new);
        registrar.play(PacketUpdateItemViewer.ID, PacketUpdateItemViewer::new);
        killItemViewer = registrar.playInstanced(Mekanism.rl("kill_qio"), context -> {
            QIOItemViewerContainer container = PacketUtils.container(context, QIOItemViewerContainer.class);
            if (container != null) {
                container.handleKill();
            }
        });

        //Transmitters
        registrar.play(PacketNetworkScale.ID, PacketNetworkScale::new);
        registrar.play(PacketChemicalNetworkContents.ID, PacketChemicalNetworkContents::new);
        registrar.play(PacketFluidNetworkContents.ID, PacketFluidNetworkContents::new);
        registrar.play(PacketTransporterBatch.ID, PacketTransporterBatch::new);
        registrar.play(PacketTransporterSync.ID, PacketTransporterSync::new);
    }
}