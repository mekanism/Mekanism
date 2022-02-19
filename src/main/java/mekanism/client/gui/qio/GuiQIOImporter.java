package mekanism.client.gui.qio;

import mekanism.client.gui.element.GuiScreenSwitch;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.qio.TileEntityQIOImporter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiQIOImporter extends GuiQIOFilterHandler<TileEntityQIOImporter> {

    public GuiQIOImporter(MekanismTileContainer<TileEntityQIOImporter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addButton(new GuiScreenSwitch(this, 9, 122, imageWidth - 18, MekanismLang.QIO_IMPORT_WITHOUT_FILTER.translate(), tile::getImportWithoutFilter,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.QIO_TOGGLE_IMPORT_WITHOUT_FILTER, tile))));
    }
}