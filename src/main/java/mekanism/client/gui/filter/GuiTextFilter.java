package mekanism.client.gui.filter;

import mekanism.api.text.EnumColor;
import mekanism.client.gui.element.button.MekanismButton;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiTextFilter<FILTER extends IFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>, CONTAINER extends
      FilterContainer<FILTER, TILE>> extends GuiTextFilterBase<FILTER, TILE, CONTAINER> {

    protected MekanismButton checkboxButton;

    protected GuiTextFilter(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    @Override
    protected boolean wasTextboxKey(char c, int i) {
        return TransporterFilter.SPECIAL_CHARS.contains(c) || super.wasTextboxKey(c, i);
    }

    @Override
    protected TextFieldWidget createTextField() {
        return new TextFieldWidget(font, getGuiLeft() + 35, getGuiTop() + 47, 95, 12, "");
    }

    @Override
    public void tick() {
        super.tick();
        if (ticker > 0) {
            ticker--;
        } else {
            status = MekanismLang.STATUS_OK.translateColored(EnumColor.DARK_GREEN);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (tile instanceof TileEntityDigitalMiner) {
            drawMinerForegroundLayer();
        } else if (tile instanceof TileEntityLogisticalSorter) {
            drawTransporterForegroundLayer();
        }
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        text.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && tile instanceof TileEntityDigitalMiner && filter instanceof MinerFilter) {
            minerFilterClickCommon(mouseX - getGuiLeft(), mouseY - getGuiTop(), (MinerFilter<?>) filter);
        }
        return true;
    }
}