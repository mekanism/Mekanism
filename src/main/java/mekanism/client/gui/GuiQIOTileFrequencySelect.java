package mekanism.client.gui;

import java.util.List;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.IQIOFrequencyHolder;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.frequency.Frequency.FrequencyIdentity;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.inventory.container.tile.QIOFrequencySelectTileContainer;
import mekanism.common.network.PacketGuiSetFrequency;
import mekanism.common.network.PacketGuiSetFrequency.FrequencyUpdate;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiQIOTileFrequencySelect extends GuiQIOFrequencySelect<QIOFrequencySelectTileContainer> {

    private IQIOFrequencyHolder tile;

    public GuiQIOTileFrequencySelect(QIOFrequencySelectTileContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        tile = (IQIOFrequencyHolder) container.getTileEntity();
    }

    @Override
    public void sendSetFrequency(FrequencyIdentity identity) {
        Mekanism.packetHandler.sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.SET_TILE, FrequencyType.QIO, identity, tile.getPos()));
    }

    @Override
    public void sendRemoveFrequency(FrequencyIdentity identity) {
        Mekanism.packetHandler.sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.REMOVE_TILE, FrequencyType.QIO, identity, tile.getPos()));
    }

    @Override
    public QIOFrequency getFrequency() {
        return tile.getQIOFrequency();
    }

    @Override
    public String getOwnerUsername() {
        return tile.getSecurity().getClientOwner();
    }

    @Override
    public UUID getOwnerUUID() {
        return tile.getSecurity().getOwnerUUID();
    }

    @Override
    public List<QIOFrequency> getPublicFrequencies() {
        return tile.getPublicFrequencies();
    }

    @Override
    public List<QIOFrequency> getPrivateFrequencies() {
        return tile.getPrivateFrequencies();
    }
}
