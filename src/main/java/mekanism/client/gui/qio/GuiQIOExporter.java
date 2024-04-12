package mekanism.client.gui.qio;

import java.util.List;
import mekanism.client.gui.element.GuiScreenSwitch;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class GuiQIOExporter extends GuiQIOFilterHandler<TileEntityQIOExporter> {

    private static final int GAP = 1;

    public GuiQIOExporter(MekanismTileContainer<TileEntityQIOExporter> container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void addGuiElements() {
        super.addGuiElements();
        int toggleWidth = (imageWidth - 18) / 2 - GAP;
        addRenderableWidget(new GuiScreenSwitch(this, 9, 122, toggleWidth, MekanismLang.QIO_EXPORT_WITHOUT_FILTER.translate(), tile::getExportWithoutFilter,
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.QIO_TOGGLE_EXPORT_WITHOUT_FILTER, ((GuiQIOExporter) element.gui()).tile))));
        List<Component> rrDescription = List.of(MekanismLang.QIO_EXPORTER_ROUND_ROBIN_DESCRIPTION.translate());
        addRenderableWidget(new GuiScreenSwitch(this, 9 + toggleWidth + 2 * GAP, 122, toggleWidth, MekanismLang.QIO_EXPORTER_ROUND_ROBIN.translate(), tile::getRoundRobin,
              (element, mouseX, mouseY) -> PacketUtils.sendToServer(new PacketGuiInteract(GuiInteraction.ROUND_ROBIN_BUTTON, ((GuiQIOExporter) element.gui()).tile))))
              .tooltip(() -> rrDescription);
    }
}