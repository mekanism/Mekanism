package mekanism.client.gui.filter;

import mekanism.client.gui.element.GuiTextField;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.inventory.container.tile.filter.FilterContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiTextFilterBase<FILTER extends IFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>, CONTAINER extends
      FilterContainer<FILTER, TILE>> extends GuiFilterBase<FILTER, TILE, CONTAINER> {

    protected GuiTextField text;

    protected GuiTextFilterBase(CONTAINER container, PlayerInventory inv, ITextComponent title) {
        super(container, inv, title);
    }

    protected abstract void setText();

    protected abstract GuiTextField createTextField();

    protected boolean wasTextboxKey(char c) {
        return Character.isLetter(c) || Character.isDigit(c);
    }

    @Override
    public void init() {
        super.init();
        addButton(text = createTextField());
        text.setInputValidator(this::wasTextboxKey);
        text.setMaxStringLength(TransporterFilter.MAX_LENGTH);
        text.setEnterHandler(this::setText);
        text.changeFocus(true);
    }
}