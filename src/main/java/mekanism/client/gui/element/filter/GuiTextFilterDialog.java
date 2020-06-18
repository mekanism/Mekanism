package mekanism.client.gui.element.filter;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.functions.CharPredicate;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInnerScreen;
import mekanism.client.gui.element.button.TranslationButton;
import mekanism.client.gui.element.slot.GuiSequencedSlotDisplay;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.text.InputValidator;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public abstract class GuiTextFilterDialog<FILTER extends IFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>> extends GuiFilterDialog<FILTER> {

    protected GuiTextField text;
    protected GuiSequencedSlotDisplay slotDisplay;
    protected TILE tile;

    protected GuiTextFilterDialog(IGuiWrapper gui, int x, int y, int width, int height, ITextComponent filterName, TILE tile, FILTER origFilter) {
        super(gui, x, y, width, height, filterName, origFilter);
        this.tile = tile;
        init();
        if (filter.hasFilter()) {
            slotDisplay.updateStackList();
        }
    }

    protected int getScreenHeight() {
        return 43;
    }

    protected int getSlotOffset() {
        return 18;
    }

    protected void init() {
        int screenTop = relativeY + 18;
        int screenBottom = screenTop + getScreenHeight();
        addChild(new GuiSlot(SlotType.NORMAL, guiObj, relativeX + 7, relativeY + getSlotOffset()).setRenderHover(true));
        addChild(new GuiInnerScreen(guiObj, relativeX + 29, screenTop, width - 29 - 7, getScreenHeight(), this::getScreenText).clearFormat());
        addChild(new TranslationButton(guiObj, x + width / 2 - 61, guiObj.getTop() + screenBottom + 2, 60, 20,
              isNew ? MekanismLang.BUTTON_CANCEL : MekanismLang.BUTTON_DELETE, () -> {
            if (origFilter != null) {
                Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), true, origFilter, null));
            }
            close();
        }));
        addChild(new TranslationButton(guiObj, x + width / 2 + 1, guiObj.getTop() + screenBottom + 2, 60, 20,
              MekanismLang.BUTTON_SAVE, () -> {
            if (!text.getText().isEmpty()) {
                setText();
            }
            if (filter.hasFilter()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(tile.getPos(), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), false, origFilter, filter));
                }
                close();
            } else {
                status = getNoFilterSaveError().translateColored(EnumColor.DARK_RED);
                ticker = 20;
            }
        }));
        addChild(slotDisplay = new GuiSequencedSlotDisplay(guiObj, relativeX + 8, relativeY + getSlotOffset() + 1, this::getRenderStacks));
        addChild(text = new GuiTextField(guiObj, relativeX + 31, screenBottom - 14, width - 31 - 9, 12));
        text.setMaxStringLength(TransporterFilter.MAX_LENGTH);
        text.setInputValidator(getInputValidator());
        text.setEnabled(true);
        text.setFocused(true);
        text.configureDigitalInput(this::setText);
    }

    protected List<ITextComponent> getScreenText() {
        List<ITextComponent> list = new ArrayList<>();
        list.add(MekanismLang.STATUS.translate(status));
        return list;
    }

    protected CharPredicate getInputValidator() {
        return InputValidator.or(InputValidator.LETTER, InputValidator.DIGIT, InputValidator.FILTER_CHARS);
    }

    protected abstract ILangEntry getNoFilterSaveError();

    protected abstract void setText();

    @Override
    public void tick() {
        super.tick();
        if (ticker > 0) {
            ticker--;
        } else {
            status = MekanismLang.STATUS_OK.translateColored(EnumColor.DARK_GREEN);
        }
    }

    protected abstract List<ItemStack> getRenderStacks();
}
