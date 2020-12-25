package mekanism.client.gui.qio;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.IQIOFrequencyHolder;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.tile.QIOFrequencySelectTileContainer;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.network.PacketGuiButtonPress;
import mekanism.common.network.PacketGuiButtonPress.ClickedTileButton;
import mekanism.common.network.PacketGuiSetFrequency;
import mekanism.common.network.PacketGuiSetFrequency.FrequencyUpdate;
import mekanism.common.network.PacketQIOSetColor;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;

public class GuiQIOTileFrequencySelect extends GuiQIOFrequencySelect<QIOFrequencySelectTileContainer> {

    private final IQIOFrequencyHolder tile;

    public GuiQIOTileFrequencySelect(QIOFrequencySelectTileContainer container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
        dynamicSlots = true;
        tile = (IQIOFrequencyHolder) container.getTileEntity();
    }

    @Override
    public void init() {
        super.init();
        addButton(new MekanismImageButton(this, guiLeft + 6, guiTop + 6, 14, getButtonLocation("back"),
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiButtonPress(ClickedTileButton.BACK_BUTTON, (TileEntity) tile))));
    }

    @Override
    public void sendSetFrequency(FrequencyIdentity identity) {
        Mekanism.packetHandler.sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.SET_TILE, FrequencyType.QIO, identity, tile.getTilePos()));
    }

    @Override
    public void sendRemoveFrequency(FrequencyIdentity identity) {
        Mekanism.packetHandler.sendToServer(PacketGuiSetFrequency.create(FrequencyUpdate.REMOVE_TILE, FrequencyType.QIO, identity, tile.getTilePos()));
    }

    @Override
    public void sendColorUpdate(int extra) {
        QIOFrequency freq = getFrequency();
        if (freq != null) {
            Mekanism.packetHandler.sendToServer(PacketQIOSetColor.create(tile.getTilePos(), freq, extra));
        }
    }

    @Nullable
    @Override
    public QIOFrequency getFrequency() {
        return tile.getQIOFrequency();
    }

    @Override
    public String getOwnerUsername() {
        return tile.getOwnerName();
    }

    @Override
    public UUID getOwnerUUID() {
        return tile.getOwnerUUID();
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
