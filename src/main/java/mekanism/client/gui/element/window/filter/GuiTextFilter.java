package mekanism.client.gui.element.window.filter;

import javax.annotation.Nullable;
import mekanism.api.functions.CharPredicate;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiTextFilter<FILTER extends IFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
      extends GuiFilter<FILTER, TILE> {

    protected GuiTextField text;

    protected GuiTextFilter(IGuiWrapper gui, int x, int y, int width, int height, ITextComponent filterName, TILE tile, @Nullable FILTER origFilter) {
        super(gui, x, y, width, height, filterName, tile, origFilter);
    }

    @Override
    protected void init() {
        super.init();
        text = addChild(new GuiTextField(gui(), relativeX + 31, relativeY + 4 + getScreenHeight(), getScreenWidth() - 4, 12));
        text.setMaxStringLength(SorterFilter.MAX_LENGTH);
        text.setInputValidator(getInputValidator());
        text.setEnabled(true);
        text.setFocused(true);
        text.configureDigitalInput(this::setText);
    }

    @Override
    protected void validateAndSave() {
        if (text.getText().isEmpty() || setText()) {
            super.validateAndSave();
        }
    }

    protected CharPredicate getInputValidator() {
        //TODO - 10.1: Re-evaluate this I am not sure all the extra filter chars are valid for things like tag filters or even really modid filters
        return InputValidator.or(InputValidator.LETTER, InputValidator.DIGIT, InputValidator.FILTER_CHARS);
    }

    /**
     * @return {@code true} if it was able to set the text because it is valid, {@code false} if an error occurred.
     */
    protected abstract boolean setText();
}