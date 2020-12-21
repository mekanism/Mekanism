package mekanism.common.network;

import mekanism.common.Mekanism;
import mekanism.common.network.container.PacketUpdateContainer;
import mekanism.common.network.container.PacketUpdateContainerBatch;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler extends BasePacketHandler {

    private static final SimpleChannel netHandler = createChannel(Mekanism.rl(Mekanism.MODID));

    @Override
    protected SimpleChannel getChannel() {
        return netHandler;
    }

    @Override
    public void initialize() {
        //TODO - 10.1: Refactor our network package so packets are in a package based on which side they go to
        // this will make it easier/cleaner to keep track of which packet goes which direction
        //Client to server messages
        registerClientToServer(PacketRobit.class, PacketRobit::encode, PacketRobit::decode, PacketRobit::handle);
        registerClientToServer(PacketModeChange.class, PacketModeChange::encode, PacketModeChange::decode, PacketModeChange::handle);
        registerClientToServer(PacketRadialModeChange.class, PacketRadialModeChange::encode, PacketRadialModeChange::decode, PacketRadialModeChange::handle);
        registerClientToServer(PacketNewFilter.class, PacketNewFilter::encode, PacketNewFilter::decode, PacketNewFilter::handle);
        registerClientToServer(PacketEditFilter.class, PacketEditFilter::encode, PacketEditFilter::decode, PacketEditFilter::handle);
        registerClientToServer(PacketConfigurationUpdate.class, PacketConfigurationUpdate::encode, PacketConfigurationUpdate::decode, PacketConfigurationUpdate::handle);
        registerClientToServer(PacketKey.class, PacketKey::encode, PacketKey::decode, PacketKey::handle);
        registerClientToServer(PacketDropperUse.class, PacketDropperUse::encode, PacketDropperUse::decode, PacketDropperUse::handle);
        registerClientToServer(PacketGearStateUpdate.class, PacketGearStateUpdate::encode, PacketGearStateUpdate::decode, PacketGearStateUpdate::handle);
        registerClientToServer(PacketGuiButtonPress.class, PacketGuiButtonPress::encode, PacketGuiButtonPress::decode, PacketGuiButtonPress::handle);
        registerClientToServer(PacketGuiInteract.class, PacketGuiInteract::encode, PacketGuiInteract::decode, PacketGuiInteract::handle);
        registerClientToServer(PacketGuiSetEnergy.class, PacketGuiSetEnergy::encode, PacketGuiSetEnergy::decode, PacketGuiSetEnergy::handle);
        registerClientToServer(PacketGuiSetFrequency.class, PacketGuiSetFrequency::encode, PacketGuiSetFrequency::decode, PacketGuiSetFrequency::handle);
        registerClientToServer(PacketAddTrusted.class, PacketAddTrusted::encode, PacketAddTrusted::decode, PacketAddTrusted::handle);
        registerClientToServer(PacketSecurityMode.class, PacketSecurityMode::encode, PacketSecurityMode::decode, PacketSecurityMode::handle);
        registerClientToServer(PacketPortableTeleporter.class, PacketPortableTeleporter::encode, PacketPortableTeleporter::decode, PacketPortableTeleporter::handle);
        registerClientToServer(PacketOpenGui.class, PacketOpenGui::encode, PacketOpenGui::decode, PacketOpenGui::handle);
        registerClientToServer(PacketRemoveModule.class, PacketRemoveModule::encode, PacketRemoveModule::decode, PacketRemoveModule::handle);
        registerClientToServer(PacketUpdateInventorySlot.class, PacketUpdateInventorySlot::encode, PacketUpdateInventorySlot::decode, PacketUpdateInventorySlot::handle);
        registerClientToServer(PacketPortableTeleporterGui.class, PacketPortableTeleporterGui::encode, PacketPortableTeleporterGui::decode, PacketPortableTeleporterGui::handle);
        registerClientToServer(PacketQIOItemViewerSlotInteract.class, PacketQIOItemViewerSlotInteract::encode, PacketQIOItemViewerSlotInteract::decode, PacketQIOItemViewerSlotInteract::handle);
        registerClientToServer(PacketGuiItemDataRequest.class, PacketGuiItemDataRequest::encode, PacketGuiItemDataRequest::decode, PacketGuiItemDataRequest::handle);
        registerClientToServer(PacketQIOSetColor.class, PacketQIOSetColor::encode, PacketQIOSetColor::decode, PacketQIOSetColor::handle);
        registerClientToServer(PacketTeleporterSetColor.class, PacketTeleporterSetColor::encode, PacketTeleporterSetColor::decode, PacketTeleporterSetColor::handle);
        registerClientToServer(PacketQIOCraftingWindowSelect.class, PacketQIOCraftingWindowSelect::encode, PacketQIOCraftingWindowSelect::decode, PacketQIOCraftingWindowSelect::handle);

        //Server to client messages
        registerServerToClient(PacketTransmitterUpdate.class, PacketTransmitterUpdate::encode, PacketTransmitterUpdate::decode, PacketTransmitterUpdate::handle);
        registerServerToClient(PacketTransporterUpdate.class, PacketTransporterUpdate::encode, PacketTransporterUpdate::decode, PacketTransporterUpdate::handle);
        registerServerToClient(PacketPortalFX.class, PacketPortalFX::encode, PacketPortalFX::decode, PacketPortalFX::handle);
        registerServerToClient(PacketLaserHitBlock.class, PacketLaserHitBlock::encode, PacketLaserHitBlock::decode, PacketLaserHitBlock::handle);
        registerServerToClient(PacketLightningRender.class, PacketLightningRender::encode, PacketLightningRender::decode, PacketLightningRender::handle);
        registerServerToClient(PacketUpdateTile.class, PacketUpdateTile::encode, PacketUpdateTile::decode, PacketUpdateTile::handle);
        registerServerToClient(PacketPlayerData.class, PacketPlayerData::encode, PacketPlayerData::decode, PacketPlayerData::handle);
        registerServerToClient(PacketClearRecipeCache.class, PacketClearRecipeCache::encode, PacketClearRecipeCache::decode, PacketClearRecipeCache::handle);
        registerServerToClient(PacketSecurityUpdate.class, PacketSecurityUpdate::encode, PacketSecurityUpdate::decode, PacketSecurityUpdate::handle);
        registerServerToClient(PacketRadiationData.class, PacketRadiationData::encode, PacketRadiationData::decode, PacketRadiationData::handle);
        registerServerToClient(PacketResetPlayerClient.class, PacketResetPlayerClient::encode, PacketResetPlayerClient::decode, PacketResetPlayerClient::handle);
        registerServerToClient(PacketPortableTeleporter.class, PacketPortableTeleporter::encode, PacketPortableTeleporter::decode, PacketPortableTeleporter::handle);
        registerServerToClient(PacketFrequencyItemGuiUpdate.class, PacketFrequencyItemGuiUpdate::encode, PacketFrequencyItemGuiUpdate::decode, PacketFrequencyItemGuiUpdate::handle);
        registerServerToClient(PacketQIOItemViewerGuiSync.class, PacketQIOItemViewerGuiSync::encode, PacketQIOItemViewerGuiSync::decode, PacketQIOItemViewerGuiSync::handle);
        registerServerToClient(PacketFlyingSync.class, PacketFlyingSync::encode, PacketFlyingSync::decode, PacketFlyingSync::handle);
        registerServerToClient(PacketStepHeightSync.class, PacketStepHeightSync::encode, PacketStepHeightSync::decode, PacketStepHeightSync::handle);

        //Register container sync packet
        registerServerToClient(PacketUpdateContainer.class, PacketUpdateContainer::encode, PacketUpdateContainer::decode, PacketUpdateContainer::handle);
        //Container sync packet that batches multiple changes into one packet
        registerServerToClient(PacketUpdateContainerBatch.class, PacketUpdateContainerBatch::encode, PacketUpdateContainerBatch::decode, PacketUpdateContainerBatch::handle);
    }
}