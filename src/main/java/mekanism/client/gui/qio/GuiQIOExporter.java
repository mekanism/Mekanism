package mekanism.client.gui.qio;

import mekanism.client.gui.element.GuiScreenSwitch;
import mekanism.client.gui.element.tab.GuiQIOFrequencyTab;
import mekanism.client.gui.element.tab.GuiRedstoneControlTab;
import mekanism.client.gui.element.tab.GuiSecurityTab;
import mekanism.client.gui.element.tab.GuiUpgradeTab;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.network.PacketGuiInteract;
import mekanism.common.network.PacketGuiInteract.GuiInteraction;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public class GuiQIOExporter extends GuiQIOFilterHandler<TileEntityQIOExporter> {

    public GuiQIOExporter(MekanismTileContainer<TileEntityQIOExporter> container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    public void init() {
        super.init();
        addButton(new GuiQIOFrequencyTab(this, tile));
        addButton(new GuiSecurityTab<>(this, tile));
        addButton(new GuiRedstoneControlTab(this, tile));
        addButton(new GuiUpgradeTab(this, tile));
        addButton(new GuiScreenSwitch(this, 9, 122, xSize - 18, MekanismLang.QIO_EXPORT_WITHOUT_FILTER.translate(), tile::getExportWithoutFilter,
              () -> Mekanism.packetHandler.sendToServer(new PacketGuiInteract(GuiInteraction.QIO_TOGGLE_EXPORT_WITHOUT_FILTER, tile))));
    }
}