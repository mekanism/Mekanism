package mekanism.client.gui.qio;

import mekanism.client.gui.element.GuiScreenSwitch;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.qio.TileEntityQIOImporter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiQIOImporter extends GuiQIOFilterHandler<TileEntityQIOImporter> {

    public GuiQIOImporter(MekanismTileContainer<TileEntityQIOImporter> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        addRenderableWidget(new GuiScreenSwitch(this, 9, 122, imageWidth - 18, MekanismLang.QIO_IMPORT_WITHOUT_FILTER.translate(), tile::getImportWithoutFilter,
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.QIO_TOGGLE_IMPORT_WITHOUT_FILTER, ((GuiQIOImporter) element.gui()).tile))));
    }
}