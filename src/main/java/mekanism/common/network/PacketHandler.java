package mekanism.common.network;

import java.util.function.Function;
import mekanism.common.Mekanism;
import mekanism.common.network.container.PacketUpdateContainer;
import mekanism.common.network.container.PacketUpdateContainerBatch;
import mekanism.common.network.container.PacketUpdateContainerBlockPos;
import mekanism.common.network.container.PacketUpdateContainerBoolean;
import mekanism.common.network.container.PacketUpdateContainerByte;
import mekanism.common.network.container.PacketUpdateContainerDouble;
import mekanism.common.network.container.PacketUpdateContainerFloat;
import mekanism.common.network.container.PacketUpdateContainerFloatingLong;
import mekanism.common.network.container.PacketUpdateContainerFluidStack;
import mekanism.common.network.container.PacketUpdateContainerFrequency;
import mekanism.common.network.container.PacketUpdateContainerInt;
import mekanism.common.network.container.PacketUpdateContainerItemStack;
import mekanism.common.network.container.PacketUpdateContainerLong;
import mekanism.common.network.container.PacketUpdateContainerShort;
import mekanism.common.network.container.chemical.PacketUpdateContainerGasStack;
import mekanism.common.network.container.chemical.PacketUpdateContainerInfusionStack;
import mekanism.common.network.container.chemical.PacketUpdateContainerPigmentStack;
import mekanism.common.network.container.chemical.PacketUpdateContainerSlurryStack;
import mekanism.common.network.container.list.PacketUpdateContainerFilterList;
import mekanism.common.network.container.list.PacketUpdateContainerFrequencyList;
import mekanism.common.network.container.list.PacketUpdateContainerStringList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler extends BasePacketHandler {

    private static final SimpleChannel netHandler = createChannel(Mekanism.rl(Mekanism.MODID));

    @Override
    protected SimpleChannel getChannel() {
        return netHandler;
    }

    @Override
    public void initialize() {
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

        //Server to client messages
        registerServerToClient(PacketTransmitterUpdate.class, PacketTransmitterUpdate::encode, PacketTransmitterUpdate::decode, PacketTransmitterUpdate::handle);
        registerServerToClient(PacketTransporterUpdate.class, PacketTransporterUpdate::encode, PacketTransporterUpdate::decode, PacketTransporterUpdate::handle);
        registerServerToClient(PacketPortalFX.class, PacketPortalFX::encode, PacketPortalFX::decode, PacketPortalFX::handle);
        registerServerToClient(PacketLaserHitBlock.class, PacketLaserHitBlock::encode, PacketLaserHitBlock::decode, PacketLaserHitBlock::handle);
        registerServerToClient(PacketUpdateTile.class, PacketUpdateTile::encode, PacketUpdateTile::decode, PacketUpdateTile::handle);
        registerServerToClient(PacketPlayerData.class, PacketPlayerData::encode, PacketPlayerData::decode, PacketPlayerData::handle);
        registerServerToClient(PacketMekanismTags.class, PacketMekanismTags::encode, PacketMekanismTags::decode, PacketMekanismTags::handle);
        registerServerToClient(PacketClearRecipeCache.class, PacketClearRecipeCache::encode, PacketClearRecipeCache::decode, PacketClearRecipeCache::handle);
        registerServerToClient(PacketSecurityUpdate.class, PacketSecurityUpdate::encode, PacketSecurityUpdate::decode, PacketSecurityUpdate::handle);
        registerServerToClient(PacketRadiationData.class, PacketRadiationData::encode, PacketRadiationData::decode, PacketRadiationData::handle);
        registerServerToClient(PacketResetPlayerClient.class, PacketResetPlayerClient::encode, PacketResetPlayerClient::decode, PacketResetPlayerClient::handle);
        registerServerToClient(PacketPortableTeleporter.class, PacketPortableTeleporter::encode, PacketPortableTeleporter::decode, PacketPortableTeleporter::handle);
        registerServerToClient(PacketFrequencyItemGuiUpdate.class, PacketFrequencyItemGuiUpdate::encode, PacketFrequencyItemGuiUpdate::decode, PacketFrequencyItemGuiUpdate::handle);
        registerServerToClient(PacketQIOItemViewerGuiSync.class, PacketQIOItemViewerGuiSync::encode, PacketQIOItemViewerGuiSync::decode, PacketQIOItemViewerGuiSync::handle);

        //Register the different sync packets for containers
        registerUpdateContainer(PacketUpdateContainerBoolean.class, PacketUpdateContainerBoolean::decode);
        registerUpdateContainer(PacketUpdateContainerByte.class, PacketUpdateContainerByte::decode);
        registerUpdateContainer(PacketUpdateContainerDouble.class, PacketUpdateContainerDouble::decode);
        registerUpdateContainer(PacketUpdateContainerFloat.class, PacketUpdateContainerFloat::decode);
        registerUpdateContainer(PacketUpdateContainerInt.class, PacketUpdateContainerInt::decode);
        registerUpdateContainer(PacketUpdateContainerLong.class, PacketUpdateContainerLong::decode);
        registerUpdateContainer(PacketUpdateContainerShort.class, PacketUpdateContainerShort::decode);
        registerUpdateContainer(PacketUpdateContainerItemStack.class, PacketUpdateContainerItemStack::decode);
        registerUpdateContainer(PacketUpdateContainerFluidStack.class, PacketUpdateContainerFluidStack::decode);
        registerUpdateContainer(PacketUpdateContainerGasStack.class, PacketUpdateContainerGasStack::decode);
        registerUpdateContainer(PacketUpdateContainerInfusionStack.class, PacketUpdateContainerInfusionStack::decode);
        registerUpdateContainer(PacketUpdateContainerPigmentStack.class, PacketUpdateContainerPigmentStack::decode);
        registerUpdateContainer(PacketUpdateContainerSlurryStack.class, PacketUpdateContainerSlurryStack::decode);
        registerUpdateContainer(PacketUpdateContainerFrequency.class, PacketUpdateContainerFrequency::decode);
        registerUpdateContainer(PacketUpdateContainerFloatingLong.class, PacketUpdateContainerFloatingLong::decode);
        registerUpdateContainer(PacketUpdateContainerBlockPos.class, PacketUpdateContainerBlockPos::decode);
        //List sync packets
        registerUpdateContainer(PacketUpdateContainerStringList.class, PacketUpdateContainerStringList::decode);
        registerUpdateContainer(PacketUpdateContainerFilterList.class, PacketUpdateContainerFilterList::decode);
        registerUpdateContainer(PacketUpdateContainerFrequencyList.class, PacketUpdateContainerFrequencyList::decode);
        //Container sync packet that batches multiple changes into one packet
        registerServerToClient(PacketUpdateContainerBatch.class, PacketUpdateContainerBatch::encode, PacketUpdateContainerBatch::decode, PacketUpdateContainerBatch::handle);
    }

    private <MSG extends PacketUpdateContainer<?>> void registerUpdateContainer(Class<MSG> type, Function<PacketBuffer, MSG> decoder) {
        registerServerToClient(type, PacketUpdateContainer::encode, decoder, PacketUpdateContainer::handle);
    }
}