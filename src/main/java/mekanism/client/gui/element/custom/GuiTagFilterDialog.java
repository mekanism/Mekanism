package mekanism.client.gui.element.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.api.text.EnumColor;
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
import mekanism.common.base.TagCache;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.TransporterFilter;
import mekanism.common.network.PacketEditFilter;
import mekanism.common.network.PacketNewFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class GuiTagFilterDialog extends GuiFilterDialog<QIOTagFilter> {

    protected GuiTextField text;
    protected GuiSequencedSlotDisplay slotDisplay;

    private <TILE extends TileEntityMekanism & ITileFilterHolder<QIOFilter<?>>>
    GuiTagFilterDialog(IGuiWrapper gui, int x, int y, TILE tile, QIOTagFilter origFilter) {
        super(gui, x, y, 152, 90, MekanismLang.TAG_FILTER.translate(), origFilter);

        addChild(new GuiSlot(SlotType.NORMAL, gui, relativeX + 7, relativeY + 18).setRenderHover(true));
        addChild(new GuiInnerScreen(gui, relativeX + 29, relativeY + 18, width - 29 - 7, 43, () -> {
            List<ITextComponent> list = new ArrayList<>();
            list.add(MekanismLang.STATUS.translate(status));
            list.add(MekanismLang.TAG_FILTER_TAG.translate(filter.getTagName()));
            return list;
        }).clearFormat());
        addChild(new TranslationButton(gui, gui.getLeft() + relativeX + width / 2 - 61, gui.getTop() + relativeY + 63, 60, 20, isNew ? MekanismLang.BUTTON_CANCEL : MekanismLang.BUTTON_DELETE, () -> {
            if (origFilter != null) {
                Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), true, origFilter, null));
            }
            close();
        }));
        addChild(new TranslationButton(gui, gui.getLeft() + relativeX + width / 2 + 1, gui.getTop() + relativeY + 63, 60, 20, MekanismLang.BUTTON_SAVE, () -> {
            if (!text.getText().isEmpty()) {
                setText();
            }
            if (filter.getTagName() != null && !filter.getTagName().isEmpty()) {
                if (isNew) {
                    Mekanism.packetHandler.sendToServer(new PacketNewFilter(tile.getPos(), filter));
                } else {
                    Mekanism.packetHandler.sendToServer(new PacketEditFilter(tile.getPos(), false, origFilter, filter));
                }
                close();
            } else {
                status = MekanismLang.TAG_FILTER_NO_TAG.translateColored(EnumColor.DARK_RED);
                ticker = 20;
            }
        }));
        addChild(slotDisplay = new GuiSequencedSlotDisplay(gui, relativeX + 8, relativeY + 19, this::getRenderStacks));

        addChild(text = new GuiTextField(gui, relativeX + 31, relativeY + 47, width - 31 - 9, 12));
        text.setMaxStringLength(TransporterFilter.MAX_LENGTH);
        text.setInputValidator(InputValidator.or(InputValidator.LETTER, InputValidator.DIGIT, InputValidator.FILTER_CHARS));
        text.setEnabled(true);
        text.setFocused(true);
        text.configureDigitalInput(this::setText);

        if (filter.getTagName() != null && !filter.getTagName().isEmpty()) {
            slotDisplay.updateStackList();
        }
    }

    public static <TILE extends TileEntityMekanism & ITileFilterHolder<QIOFilter<?>>> GuiTagFilterDialog create(IGuiWrapper gui, TILE tile) {
        return new GuiTagFilterDialog(gui, gui.getWidth() / 2 - 152 / 2, 15, tile, null);
    }

    public static <TILE extends TileEntityMekanism & ITileFilterHolder<QIOFilter<?>>> GuiTagFilterDialog edit(IGuiWrapper gui, TILE tile, QIOTagFilter filter) {
        return new GuiTagFilterDialog(gui, gui.getWidth() / 2 - 152 / 2, 15, tile, filter);
    }

    @Override
    public QIOTagFilter createNewFilter() {
        return new QIOTagFilter();
    }

    protected void setText() {
        String name = text.getText();
        if (name.isEmpty()) {
            status = MekanismLang.TAG_FILTER_NO_TAG.translateColored(EnumColor.DARK_RED);
            ticker = 20;
            return;
        } else if (name.equals(filter.getTagName())) {
            status = MekanismLang.TAG_FILTER_SAME_TAG.translateColored(EnumColor.DARK_RED);
            ticker = 20;
            return;
        }
        filter.setTagName(name);
        slotDisplay.updateStackList();
        text.setText("");
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

    private List<ItemStack> getRenderStacks() {
        if (filter.getTagName() == null || filter.getTagName().isEmpty()) {
            return Collections.emptyList();
        }
        return TagCache.getItemTagStacks(filter.getTagName());
    }
}
